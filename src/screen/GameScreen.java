package screen;

import java.util.Random;
import java.awt.Color;
import java.awt.event.KeyEvent;
import java.util.HashSet;
import java.util.Set;

import engine.*;
import engine.DrawManager.SpriteType;
import entity.*;




/**
 * Implements the game screen, where the action happens.
 * 
 * @author <a href="mailto:RobertoIA1987@gmail.com">Roberto Izquierdo Amo</a>
 * 
 */
public class GameScreen extends Screen {

	/**
	 * Milliseconds until the screen accepts user input.
	 */
	private static final int INPUT_DELAY = 6000;
	/**
	 * Bonus score for each life remaining at the end of the level.
	 */
	private static final int LIFE_SCORE = 100;
	/**
	 * Movement speed of the item.
	 */
	private static final int ITEM_SPEED = 3;
	/**
	 * Minimum time between bonus ship's appearances.
	 */
	private static final int BONUS_SHIP_INTERVAL = 20000;
	/**
	 * Maximum variance in the time between bonus ship's appearances.
	 */
	private static final int BONUS_SHIP_VARIANCE = 10000;
	/**
	 * Time until bonus ship explosion disappears.
	 */
	private static final int BONUS_SHIP_EXPLOSION = 500;
	/**
	 * Time from finishing the level to screen change.
	 */
	private static final int SCREEN_CHANGE_INTERVAL = 1500;
	/**
	 * Height of the interface separation line.
	 */
	private static final int SEPARATION_LINE_HEIGHT = 40;


	private static final int SEPARATION_LINE_WIDTH = Core.getWIDTH() / 2;

	/**
	 * Current game difficulty settings.
	 */
	private GameSettings gameSettings;
	/**
	 * Current difficulty level number.
	 */
	private int level;
	/**
	 * Formation of enemy ships.
	 */
	private EnemyShipFormation enemyShipFormation;

	private EnemyShipFormation enemyShipFormationL;
	private EnemyShipFormation enemyShipFormationR;
	/**
	 * Player's ship.
	 */
	private Ship ship;

	private Ship shipR;

	private Ship shipL;
	/**
	 * Player's ship width.
	 */
	private int shipWidth = 13*2;
	/**
	 * Bonus enemy ship that appears sometimes.
	 */
	private EnemyShip enemyShipSpecial;
	/**
	 * Minimum time between bonus ship appearances.
	 */
	private Cooldown enemyShipSpecialCooldown;
	/**
	 * Time until bonus ship explosion disappears.
	 */
	private Cooldown enemyShipSpecialExplosionCooldown;
	/**
	 * Time from finishing the level to screen change.
	 */
	private Cooldown screenFinishedCooldown;
	/**
	 * Set of all bullets fired by on screen ships.
	 */
	private Set<Bullet> bullets;

	private Set<BulletN> bulletsN;

	private Set<BulletH> bulletsH;


	/** Current score. */
	private int score;
	/** Current coin. */
	private int coin;
	/** Player lives left. */
	public static int lives;

	public static int livesL;

	public static int livesR;
	/**
	 * Total bullets shot by the player.
	 */
	private int bulletsShot;
	/**
	 * Total ships destroyed by the player.
	 */
	private int shipsDestroyed;
	/**
	 * Moment the game starts.
	 */
	private long gameStartTime;
	/**
	 * Checks if the level is finished.
	 */
	private boolean levelFinished;
	/**
	 * Checks if a bonus life is received.
	 */
	private boolean bonusLife;

	public int enemyLives;
	/**
	 * Set of all items dropped by on screen enemyships.
	 */

	private Set<entity.Item> items;

	/**
	 * Constructor, establishes the properties of the screen.
	 *
	 * @param gameState
	 *                     Current game state.
	 * @param gameSettings
	 *                     Current game settings.
	 * @param bonusLife
	 *                     Checks if a bonus life is awarded this level.
	 * @param width
	 *                     Screen width.
	 * @param height
	 *                     Screen height.
	 * @param fps
	 *                     Frames per second, frame rate at which the game is run.
	 */
	public GameScreen(final GameState gameState,
			final GameSettings gameSettings, final boolean bonusLife,
			final int width, final int height, final int fps) {
		super(width, height, fps);

		this.gameSettings = gameSettings;
		this.bonusLife = bonusLife;
		this.level = gameState.getLevel();
		this.score = gameState.getScore();
		//this.lives = gameState.getLivesRemaining();
		this.livesL = gameState.getLivesRemainingL();
		this.livesR = gameState.getLivesRemainingR();
		this.coin = gameState.getCoin();
		this.bulletsShot = gameState.getBulletsShot();
		this.shipsDestroyed = gameState.getShipsDestroyed();
	}

	/**
	 * Initializes basic screen properties, and adds necessary elements.
	 */
	public final void initialize() {
		super.initialize();
		enemyShipFormation = new EnemyShipFormation(this.gameSettings);
		enemyShipFormation.attach(this);
		/**
		 * enemyShipFormationL = new EnemyShipFormation(this.gameSettings);
		 * enemyShipFormation.attach(this);
		 * enemyShipFormationR = new EnemyShipFormation(this.gameSettings);
		 * enemyShipFormation.attach(this);
		 * */
		/** You can add your Ship to the code below. */
		this.shipL = new Ship(this.width / 4, this.height - 30, Color.RED);
		this.shipR = new Ship(this.width / 4 * 3, this.height - 30, Color.BLUE);

		// Appears each 10-30 seconds.
		this.enemyShipSpecialCooldown = Core.getVariableCooldown(
				BONUS_SHIP_INTERVAL, BONUS_SHIP_VARIANCE);
		this.enemyShipSpecialCooldown.reset();
		this.enemyShipSpecialExplosionCooldown = Core
				.getCooldown(BONUS_SHIP_EXPLOSION);
		this.screenFinishedCooldown = Core.getCooldown(SCREEN_CHANGE_INTERVAL);
		this.bullets = new HashSet<Bullet>();
		this.bulletsN = new HashSet<BulletN>();
		this.bulletsH = new HashSet<BulletH>();
		this.items = new HashSet<entity.Item>();

		// Special input delay / countdown.
		this.gameStartTime = System.currentTimeMillis();
		this.inputDelay = Core.getCooldown(INPUT_DELAY);
		this.inputDelay.reset();
	}

	/**
	 * Starts the action.
	 *
	 * @return Next screen code.
	 */
	public final int run() {
		super.run();

		this.score += LIFE_SCORE * (this.livesL + this.livesR - 1);
		this.logger.info("Screen cleared with a score of " + this.score);

		return this.returnCode;
	}

	/**
	 * Updates the elements on screen and checks for events.
	 */
	protected final void update() {
		super.update();

		if (this.inputDelay.checkFinished() && !this.levelFinished) {

			this.move();
			this.shipR.update();
			this.shipL.update();
			this.enemyShipFormation.update();
			//this.enemyShipFormationL.update();
			//this.enemyShipFormationR.update();
			switch (Core.getDiff()){
				case 1:
					this.enemyShipFormation.shoot(this.bullets);
					//this.enemyShipFormationL.shoot(this.bullets);
					//this.enemyShipFormationR.shoot(this.bullets);
					break;
				case 2:
					this.enemyShipFormation.shootN(this.bulletsN);
					//this.enemyShipFormationL.shootN(this.bulletsN);
					//this.enemyShipFormationR.shootN(this.bulletsN);
					break;
				case 3:
					this.enemyShipFormation.shootH(this.bulletsH);
					//this.enemyShipFormationL.shootH(this.bulletsH);
					//this.enemyShipFormationR.shootH(this.bulletsH);
					break;
			}

		}
		manageCollisions();
		manageCollisionsN();
		manageCollisionsH();
		cleanBullets();
		cleanBulletsN();
		cleanBulletsH();
		manageCollisionsItem();
		cleanItems();
		draw();
		level_finish();
	}

	private void move(){
		if (!this.shipL.isDestroyed() && !this.shipR.isDestroyed()) {
			boolean moveRightL = inputManager.isKeyDown(KeyEvent.VK_D);
			boolean moveLeftL = inputManager.isKeyDown(KeyEvent.VK_A);
			boolean moveRightR = inputManager.isKeyDown(KeyEvent.VK_RIGHT);
			boolean moveLeftR = inputManager.isKeyDown(KeyEvent.VK_LEFT);
			boolean isMiddleLine_shipL = this.shipL.getPositionX()
					+ this.shipL.getWidth() + this.shipL.getSpeed() > this.width / 2 - 1;
			boolean isLeftBorder_shipL = this.shipL.getPositionX()
					- this.shipL.getSpeed() < 1;
			boolean isRightBorder_shipR = this.shipR.getPositionX()
					+ this.shipR.getWidth() + this.shipR.getSpeed() > this.width - 1;
			boolean isMiddleLine_shipR = this.shipR.getPositionX()
					- this.shipR.getSpeed() < this.width / 2 + 1;


			if (moveRightL && !isMiddleLine_shipL) {
				this.shipL.moveRight();
			}
			if (moveLeftL && !isLeftBorder_shipL) {
				this.shipL.moveLeft();
			}
			if (moveRightR && !isRightBorder_shipR) {
				this.shipR.moveRight();
			}
			if (moveLeftR && !isMiddleLine_shipR) {
				this.shipR.moveLeft();
			}
			this.shoot();
			this.animctr();
		}
	}

	private void shoot(){
		if (inputManager.isKeyDown(KeyEvent.VK_SPACE))
			if (this.shipL.shoot(this.bullets))
				this.bulletsShot++;

		if (inputManager.isKeyDown(KeyEvent.VK_UP))
			if (this.shipR.shoot(this.bullets))
				this.bulletsShot++;
	}

	private void animctr(){
		if (inputManager.isKeyDown(KeyEvent.VK_LEFT)) {
			shipR.animctr = 2;
		} else if (inputManager.isKeyDown(KeyEvent.VK_RIGHT)) {
			shipR.animctr = 3;
		} else if (inputManager.isKeyDown(KeyEvent.VK_A)) {
			shipL.animctr = 2;
		} else if (inputManager.isKeyDown(KeyEvent.VK_D)) {
			shipL.animctr = 3;
		} else {
			shipR.animctr = 1;
			shipL.animctr = 1;
		}
	}

	// levelFinished : Gameover + level clear 둘다포함
	private void level_finish(){
		if ((this.enemyShipFormation.isEmpty() || (this.livesL <= 0 && this.livesR <= 0))
				&& !this.levelFinished) { // 게임이 안끝났는데 적이 다 죽었을 때 || 게임이 안끝났는데 양쪽 다 목숨이 0일 때
			this.levelFinished = true; // 레벨 끝내기
			this.screenFinishedCooldown.reset(); // 화면 초기화?
			if(this.livesL<=0) this.shipL.gameOver();
			if(this.livesR<=0) this.shipR.gameOver();
		}

		if ((this.livesL <= 0 || this.livesR <= 0)
				&& !this.levelFinished) { // 게임이 안끝났는데 둘중 하나 목숨이 0일 때
			// 어떤 플레이어의 목숨이 0인지 보고 걔만 게임오버 시키기
			if(this.livesL<=0) this.shipL.gameOver();
			if(this.livesR<=0) this.shipR.gameOver();
		}

//		if ((this.enemyShipFormation.isEmpty() || this.livesL != 0 && this.livesR == 0)
//				&& !this.levelFinished) {
//			this.levelFinished = true;
//			this.screenFinishedCooldown.reset();
//			if(this.livesL==0 && this.livesR==0) this.shipR.gameOver();
//		}
//		if ((this.enemyShipFormation.isEmpty() || this.livesL == 0 && this.livesR != 0)
//				&& !this.levelFinished) {
//			this.levelFinished = true;
//			this.screenFinishedCooldown.reset();
//			if(this.livesL==0 && this.livesR==0) this.shipL.gameOver();
//		}

		if (this.levelFinished && this.screenFinishedCooldown.checkFinished())
			this.isRunning = false;
	}

	/**
	 * Draws the elements associated with the screen.
	 */
	private void draw() {
		drawManager.initDrawing(this);
		drawManager.drawEntity(this.shipR, this.shipR.getPositionX(), this.shipR.getPositionY());
		drawManager.drawEntity(this.shipL, this.shipL.getPositionX(), this.shipL.getPositionY());
		if (this.enemyShipSpecial != null)
			drawManager.drawEntity(this.enemyShipSpecial,
					this.enemyShipSpecial.getPositionX(),
					this.enemyShipSpecial.getPositionY());

		enemyShipFormation.draw();

		for (Bullet bullet : this.bullets)
			drawManager.drawEntity(bullet, bullet.getPositionX(),
					bullet.getPositionY());

		for (BulletN bulletN : this.bulletsN)
			drawManager.drawEntity(bulletN, bulletN.getPositionX(),
					bulletN.getPositionY());

		for (BulletH bulletH : this.bulletsH)
			drawManager.drawEntity(bulletH, bulletH.getPositionX(),
					bulletH.getPositionY());

		for (entity.Item item : this.items)
			drawManager.drawEntity(item, item.getPositionX(),
					item.getPositionY());

		// Interface.
		drawManager.drawScore(this, this.score);
		drawManager.drawLives(this, this.livesL, 0);
		drawManager.drawLives(this, this.livesR, 1);
		drawManager.drawHorizontalLine(this, SEPARATION_LINE_HEIGHT - 1);
		drawManager.drawVerticalLine(this, SEPARATION_LINE_WIDTH - 1);
		drawManager.drawCoin(this, this.coin);

		// Countdown to game start.
		if (!this.inputDelay.checkFinished()) {
			int countdown = (int) ((INPUT_DELAY
					- (System.currentTimeMillis()
							- this.gameStartTime))
					/ 1000);
			drawManager.drawCountDown(this, this.level, countdown,
					this.bonusLife);
			drawManager.drawHorizontalLine(this, this.height / 2 - this.height
					/ 12);
			drawManager.drawHorizontalLine(this, this.height / 2 + this.height
					/ 12);
		}

		drawManager.completeDrawing(this);
	}

	/**
	 * Cleans bullets that go off screen.
	 */
	private void cleanBullets() {
		Set<Bullet> recyclable = new HashSet<Bullet>();
		for (Bullet bullet : this.bullets) {
			bullet.update();
			if (bullet.getPositionY() < SEPARATION_LINE_HEIGHT
					|| bullet.getPositionY() > this.height)
				recyclable.add(bullet);
		}
		this.bullets.removeAll(recyclable);
		BulletPool.recycle(recyclable);
	}

	private void cleanBulletsN() {
		Set<BulletN> recyclable = new HashSet<BulletN>();
		for (BulletN bulletN : this.bulletsN) {
			bulletN.update();
			if (bulletN.getPositionY() < SEPARATION_LINE_HEIGHT
					|| bulletN.getPositionY() > this.height)
				recyclable.add(bulletN);
		}
		this.bulletsN.removeAll(recyclable);
		BulletPool.recycleN(recyclable);
	}

	private void cleanBulletsH() {
		Set<BulletH> recyclable = new HashSet<BulletH>();
		for (BulletH bulletH : this.bulletsH) {
			bulletH.update();
			if (bulletH.getPositionY() < SEPARATION_LINE_HEIGHT
					|| bulletH.getPositionY() > this.height)
				recyclable.add(bulletH);
		}
		this.bulletsH.removeAll(recyclable);
		BulletPool.recycleH(recyclable);
	}

	private void cleanItems() {
		Set<entity.Item> recyclable = new HashSet<entity.Item>();
		for (entity.Item item : this.items) {
			item.update();
			if (item.getPositionY() > this.height)
				recyclable.add(item);
		}
		this.items.removeAll(recyclable);
		ItemPool.recycle(recyclable);
	}

	/**
	 * Manages collisions between bullets and ships.
	 */
	private void manageCollisions() {
		Set<Bullet> recyclable = new HashSet<Bullet>();
		for (Bullet bullet : this.bullets)
			if (bullet.getSpeed() > 0) {
				// ship -> shipL, shipR
				if (checkCollision(bullet, this.shipL) && !this.levelFinished) {
					recyclable.add(bullet);
					if (!this.shipL.isDestroyed()) {
						// Ship이 destroy 되었을 때 처리
						this.shipL.destroy();
						if(this.livesL > 0) this.livesL--;
						else{
							this.shipL.setSPEED(0);
							this.shipL.setSHOOTING_COOLDOWN(2147483647);
						}
						this.logger.info("Hit on player ship, " + this.livesL
								+ " lives remaining.");
					}
				}

				if (checkCollision(bullet, this.shipR) && !this.levelFinished) {
					recyclable.add(bullet);
					if (!this.shipR.isDestroyed()) {
						// Ship이 destroy 되었을 때 처리
						this.shipR.destroy();
						if(this.livesR > 0) this.livesR--;
						else{
							this.shipR.setSPEED(0);
							this.shipR.setSHOOTING_COOLDOWN(2147483647);
						}
						this.logger.info("Hit on player ship, " + this.livesR
								+ " lives remaining.");
					}
				}

			} else {
				for (EnemyShip enemyShip : this.enemyShipFormation)
					if (!enemyShip.isDestroyed()
							&& checkCollision(bullet, enemyShip)) {
						enemyLives = enemyShip.getEnemyLives();
						if (enemyLives == 1) {
							this.score += enemyShip.getPointValue();
							this.shipsDestroyed++;
							Random random = new Random();
							int per = random.nextInt(3);
							if (per == 0) {
								items.add(ItemPool.getItem(enemyShip.getPositionX() + enemyShip.getWidth() / 2,
										enemyShip.getPositionY(), ITEM_SPEED));
							}
							this.enemyShipFormation.destroy(enemyShip);
							this.coin += enemyShip.getPointValue() / 10;
							Coin.balance += enemyShip.getPointValue() / 10;
							recyclable.add(bullet);
						}
						else {
							enemyLives--;
							enemyShip.setenemyLives(enemyLives);
							recyclable.add(bullet);
						}
					}
				if (this.enemyShipSpecial != null
						&& !this.enemyShipSpecial.isDestroyed()
						&& checkCollision(bullet, this.enemyShipSpecial)) {
					this.score += this.enemyShipSpecial.getPointValue();
					this.shipsDestroyed++;
					this.enemyShipSpecial.destroy();
					this.enemyShipSpecialExplosionCooldown.reset();
					this.coin += this.enemyShipSpecial.getPointValue() / 10;
					Coin.balance += this.enemyShipSpecial.getPointValue() / 10;
					recyclable.add(bullet);
				}
			}
		this.bullets.removeAll(recyclable);
		BulletPool.recycle(recyclable);
	}

	private void manageCollisionsN() {
		Set<BulletN> recyclable = new HashSet<BulletN>();
		for (BulletN bullet : this.bulletsN)
			if (bullet.getSpeed() > 0) {
				// ship -> shipL, shipR
				if (checkCollision(bullet, this.shipL) && !this.levelFinished) {
					recyclable.add(bullet);
					if (!this.shipL.isDestroyed()) {
						this.shipL.destroy();
						if(this.livesL > 0) this.livesL--;
						else{
							this.shipL.setSPEED(0);
							this.shipL.setSHOOTING_COOLDOWN(2147483647);
						}
						this.logger.info("Hit on player ship, " + this.livesL
								+ " lives remaining.");
					}
				}
				if (checkCollision(bullet, this.shipR) && !this.levelFinished) {
					recyclable.add(bullet);
					if (!this.shipR.isDestroyed()) {
						this.shipR.destroy();
						if(this.livesR > 0) this.livesR--;
						else{
							this.shipR.setSPEED(0);
							this.shipR.setSHOOTING_COOLDOWN(2147483647);
						}
						this.logger.info("Hit on player ship, " + this.livesR
								+ " lives remaining.");
					}
				}
			} else {
				for (EnemyShip enemyShip : this.enemyShipFormation)
					if (!enemyShip.isDestroyed()
							&& checkCollision(bullet, enemyShip)) {
						this.score += enemyShip.getPointValue();
						this.shipsDestroyed++;
						Random random = new Random();
						int per = random.nextInt(2);
						if(per == 0){
							items.add(ItemPool.getItem(enemyShip.getPositionX() + enemyShip.getWidth() / 2,
									enemyShip.getPositionY(), ITEM_SPEED));
						}
						this.enemyShipFormation.destroy(enemyShip);
						recyclable.add(bullet);
					}
				if (this.enemyShipSpecial != null
						&& !this.enemyShipSpecial.isDestroyed()
						&& checkCollision(bullet, this.enemyShipSpecial)) {
					this.score += this.enemyShipSpecial.getPointValue();
					this.shipsDestroyed++;
					this.enemyShipSpecial.destroy();
					this.enemyShipSpecialExplosionCooldown.reset();
					recyclable.add(bullet);
				}
			}
		this.bullets.removeAll(recyclable);
		BulletPool.recycleN(recyclable);
	}

	private void manageCollisionsH() {
		Set<BulletH> recyclable = new HashSet<BulletH>();
		for (BulletH bullet : this.bulletsH) {
			if (bullet.getSpeed() > 0) {
				if (checkCollision(bullet, this.shipL) && !this.levelFinished) {
					recyclable.add(bullet);
					if (!this.shipL.isDestroyed()) {
						this.shipL.destroy();
						if(this.livesL > 0) this.livesL--;
						else{
							this.shipL.setSPEED(0);
							this.shipL.setSHOOTING_COOLDOWN(2147483647);
						}
						this.logger.info("Hit on player ship, " + this.livesL
								+ " lives remaining.");
					}
				}
			} else {
				for (EnemyShip enemyShip : this.enemyShipFormation)
					if (!enemyShip.isDestroyed()
							&& checkCollision(bullet, enemyShip)) {
						this.score += enemyShip.getPointValue();
						this.shipsDestroyed++;
						Random random = new Random();
						int per = random.nextInt(2);
						if (per == 0) {
							items.add(ItemPool.getItem(enemyShip.getPositionX() + enemyShip.getWidth() / 2,
									enemyShip.getPositionY(), ITEM_SPEED));
						}
						this.enemyShipFormation.destroy(enemyShip);
						recyclable.add(bullet);
					}
				if (this.enemyShipSpecial != null
						&& !this.enemyShipSpecial.isDestroyed()
						&& checkCollision(bullet, this.enemyShipSpecial)) {
					this.score += this.enemyShipSpecial.getPointValue();
					this.shipsDestroyed++;
					this.enemyShipSpecial.destroy();
					this.enemyShipSpecialExplosionCooldown.reset();
					recyclable.add(bullet);
				}
			}
			if(bullet.getSpeed() > 0){
				if (checkCollision(bullet, this.shipR) && !this.levelFinished) {
					recyclable.add(bullet);
					if (!this.shipR.isDestroyed()) {
						this.shipR.destroy();
						if(this.livesR > 0) this.livesR--;
						else{
							this.shipR.setSPEED(0);
							this.shipR.setSHOOTING_COOLDOWN(2147483647);
						}
						this.logger.info("Hit on player ship, " + this.livesR
								+ " lives remaining.");
					}
				}
			} else {
				for (EnemyShip enemyShip : this.enemyShipFormation)
					if (!enemyShip.isDestroyed()
							&& checkCollision(bullet, enemyShip)) {
						this.score += enemyShip.getPointValue();
						this.shipsDestroyed++;
						Random random = new Random();
						int per = random.nextInt(2);
						if (per == 0) {
							items.add(ItemPool.getItem(enemyShip.getPositionX() + enemyShip.getWidth() / 2,
									enemyShip.getPositionY(), ITEM_SPEED));
						}
						this.enemyShipFormation.destroy(enemyShip);
						recyclable.add(bullet);
					}
				if (this.enemyShipSpecial != null
						&& !this.enemyShipSpecial.isDestroyed()
						&& checkCollision(bullet, this.enemyShipSpecial)) {
					this.score += this.enemyShipSpecial.getPointValue();
					this.shipsDestroyed++;
					this.enemyShipSpecial.destroy();
					this.enemyShipSpecialExplosionCooldown.reset();
					recyclable.add(bullet);
				}
			}
		}

		this.bullets.removeAll(recyclable);
		BulletPool.recycleH(recyclable);
	}

	/**
	 * Returns a GameState object representing the status of the game.
	 *
	 * @return Current game state.
	 */
	public final GameState getGameState() {
		return new GameState(this.level, this.score, this.livesL, this.livesR,
				this.bulletsShot, this.shipsDestroyed, this.coin);
	}

	/**
	 * Manages collisions between items and ships.
	 */

	private void manageCollisionsItem() {
		Set<entity.Item> recyclable = new HashSet<entity.Item>(); // ItemPool
		for (entity.Item item : this.items) {
			if (checkCollision(item, this.shipL) && !this.levelFinished) {
				recyclable.add(item);
				Random random = new Random();
				int per = random.nextInt(6);

				if (per == 0) {
					if (this.livesL < 3) {
						this.livesL++;
						this.logger.info("Acquire a item_lifePoint," + this.livesL + " lives remaining.");
						this.shipL.item_number = 1;
						this.shipL.itemimgGet();
					}
					else {
						if (shipL.getSHOOTING_INTERVAL() > 300) {
							int shootingSpeed = (int) (shipL.getSHOOTING_INTERVAL() -100);
							shipL.setSHOOTING_INTERVAL(shootingSpeed);
							shipL.setSHOOTING_COOLDOWN(shootingSpeed);
							this.logger.info("Acquire a item_shootingSpeedUp," + shootingSpeed + " Time between shots.");
						}
						else {
							this.logger.info("Acquire a item_shootingSpeedUp, MAX SHOOTING SPEED!");
						}
						this.shipL.item_number = 2;
						this.shipL.itemimgGet();
					}
				}else if (per == 1) {
					if (shipL.getSHOOTING_INTERVAL() > 300) {
						int shootingSpeed = (int) (shipL.getSHOOTING_INTERVAL() -100);
						shipL.setSHOOTING_INTERVAL(shootingSpeed);
						shipL.setSHOOTING_COOLDOWN(shootingSpeed);
						this.logger.info("Acquire a item_shootingSpeedUp," + shootingSpeed + " Time between shots.");
					}
					else {
						this.logger.info("Acquire a item_shootingSpeedUp, MAX SHOOTING SPEED!");
					}
					this.shipL.item_number = 2;
					this.shipL.itemimgGet();
				}
				else if (per == 2) {
					int shipSpeed = (int) (shipL.getSPEED() + 1);
					shipL.setSPEED(shipSpeed);
					this.logger.info("Acquire a item_shipSpeedUp," + shipSpeed + " Movement of the ship for each unit of time.");
					this.shipL.item_number = 3;
					this.shipL.itemimgGet();
				}else if (per == 3) {
					bullets.add(BulletPool.getBullet(shipL.getPositionX(),
							shipL.getPositionY(), shipL.getBULLET_SPEED(), 0));
					bullets.add(BulletPool.getBullet(shipL.getPositionX() + shipWidth/2,
							shipL.getPositionY(), shipL.getBULLET_SPEED(), 0));
					bullets.add(BulletPool.getBullet(shipL.getPositionX() + shipWidth,
							shipL.getPositionY(), shipL.getBULLET_SPEED(), 0));
					this.logger.info("Three bullets");
				}else if (per == 4) {
					bullets.add(BulletPool.getBullet(shipL.getPositionX()+shipWidth/2,
							shipL.getPositionY(), shipL.getBULLET_SPEED(), 0));
					bullets.add(BulletPool.getBullet(shipL.getPositionX() + shipWidth/2,
							shipL.getPositionY()+shipWidth/2, shipL.getBULLET_SPEED(), 0));
					bullets.add(BulletPool.getBullet(shipL.getPositionX() + shipWidth/2,
							shipL.getPositionY()+shipWidth, shipL.getBULLET_SPEED(), 0));
					this.logger.info("Three bullets");
				}else {
					bullets.add(BulletPool.getBullet(shipL.getPositionX() - shipWidth / 2,
							shipL.getPositionY(), shipL.getBULLET_SPEED(), 0));
					bullets.add(BulletPool.getBullet(shipL.getPositionX(),
							shipL.getPositionY() - shipWidth / 3, shipL.getBULLET_SPEED(), 0));
					bullets.add(BulletPool.getBullet(shipL.getPositionX() + shipWidth / 2,
							shipL.getPositionY() - shipWidth / 2, shipL.getBULLET_SPEED(), 0));
					bullets.add(BulletPool.getBullet(shipL.getPositionX() + shipWidth,
							shipL.getPositionY() - shipWidth / 3, shipL.getBULLET_SPEED(), 0));
					bullets.add(BulletPool.getBullet(shipL.getPositionX() + shipWidth + shipWidth / 2,
							shipL.getPositionY(), shipL.getBULLET_SPEED(), 0));
					this.logger.info("Five bullets");
				}
				this.shipL.getItem();
			}


			// ------------------------------------


			if (checkCollision(item, this.shipR) && !this.levelFinished) {
				recyclable.add(item);
				Random random = new Random();
				int per = random.nextInt(6);

				if (per == 0) {
					if (this.livesR < 3) {
						this.livesR++;
						this.logger.info("Acquire a item_lifePoint," + this.livesR + " lives remaining.");
						this.shipR.item_number = 1;
						this.shipR.itemimgGet();
					}
					else {
						if (shipR.getSHOOTING_INTERVAL() > 300) {
							int shootingSpeed = (int) (shipR.getSHOOTING_INTERVAL() -100);
							shipR.setSHOOTING_INTERVAL(shootingSpeed);
							shipR.setSHOOTING_COOLDOWN(shootingSpeed);
							this.logger.info("Acquire a item_shootingSpeedUp," + shootingSpeed + " Time between shots.");
						}
						else {
							this.logger.info("Acquire a item_shootingSpeedUp, MAX SHOOTING SPEED!");
						}
						this.shipR.item_number = 2;
						this.shipR.itemimgGet();
					}
				}else if (per == 1) {
					if (shipR.getSHOOTING_INTERVAL() > 300) {
						int shootingSpeed = (int) (shipR.getSHOOTING_INTERVAL() -100);
						shipR.setSHOOTING_INTERVAL(shootingSpeed);
						shipR.setSHOOTING_COOLDOWN(shootingSpeed);
						this.logger.info("Acquire a item_shootingSpeedUp," + shootingSpeed + " Time between shots.");
					}
					else {
						this.logger.info("Acquire a item_shootingSpeedUp, MAX SHOOTING SPEED!");
					}
					this.shipR.item_number = 2;
					this.shipR.itemimgGet();
				}
				else if (per == 2) {
					int shipSpeed = (int) (shipR.getSPEED() + 1);
					shipR.setSPEED(shipSpeed);
					this.logger.info("Acquire a item_shipSpeedUp," + shipSpeed + " Movement of the ship for each unit of time.");
					this.shipR.item_number = 3;
					this.shipR.itemimgGet();
				}else if (per == 3) {
					bullets.add(BulletPool.getBullet(shipR.getPositionX(),
							shipR.getPositionY(), shipR.getBULLET_SPEED(), 0));
					bullets.add(BulletPool.getBullet(shipR.getPositionX() + shipWidth/2,
							shipR.getPositionY(), shipR.getBULLET_SPEED(), 0));
					bullets.add(BulletPool.getBullet(shipR.getPositionX() + shipWidth,
							shipR.getPositionY(), shipR.getBULLET_SPEED(), 0));
					this.logger.info("Three bullets");
				}else if (per == 4) {
					bullets.add(BulletPool.getBullet(shipR.getPositionX()+shipWidth/2,
							shipR.getPositionY(), shipR.getBULLET_SPEED(), 0));
					bullets.add(BulletPool.getBullet(shipR.getPositionX() + shipWidth/2,
							shipR.getPositionY()+shipWidth/2, shipR.getBULLET_SPEED(), 0));
					bullets.add(BulletPool.getBullet(shipR.getPositionX() + shipWidth/2,
							shipR.getPositionY()+shipWidth, shipR.getBULLET_SPEED(), 0));
					this.logger.info("Three bullets");
				}else {
					bullets.add(BulletPool.getBullet(shipR.getPositionX() - shipWidth / 2,
							shipR.getPositionY(), shipR.getBULLET_SPEED(), 0));
					bullets.add(BulletPool.getBullet(shipR.getPositionX(),
							shipR.getPositionY() - shipWidth / 3, shipR.getBULLET_SPEED(), 0));
					bullets.add(BulletPool.getBullet(shipR.getPositionX() + shipWidth / 2,
							shipR.getPositionY() - shipWidth / 2, shipR.getBULLET_SPEED(), 0));
					bullets.add(BulletPool.getBullet(shipR.getPositionX() + shipWidth,
							shipR.getPositionY() - shipWidth / 3, shipR.getBULLET_SPEED(), 0));
					bullets.add(BulletPool.getBullet(shipR.getPositionX() + shipWidth + shipWidth / 2,
							shipR.getPositionY(), shipR.getBULLET_SPEED(), 0));
					this.logger.info("Five bullets");
				}
				this.shipR.getItem();
			}
		}
		this.items.removeAll(recyclable);
		ItemPool.recycle(recyclable);
	}

	/**
	 * Checks if two entities are colliding.
	 *
	 * @param a
	 *          First entity, the bullet or item.
	 * @param b
	 *          Second entity, the ship.
	 * @return Result of the collision test.
	 */
	private boolean checkCollision(final Entity a, final Entity b) {
		// Calculate center point of the entities in both axis.
		int centerAX = a.getPositionX() + a.getWidth() / 2;
		int centerAY = a.getPositionY() + a.getHeight() / 2;
		int centerBX = b.getPositionX() + b.getWidth() / 2;
		int centerBY = b.getPositionY() + b.getHeight() / 2;
		// Calculate maximum distance without collision.
		int maxDistanceX = a.getWidth() / 2 + b.getWidth() / 2;
		int maxDistanceY = a.getHeight() / 2 + b.getHeight() / 2;
		// Calculates distance.
		int distanceX = Math.abs(centerAX - centerBX);
		int distanceY = Math.abs(centerAY - centerBY);

		return distanceX < maxDistanceX && distanceY < maxDistanceY;
	}
}
