package engine;

/**
 * Implements an object that stores the state of the game between levels.
 * 
 * @author <a href="mailto:RobertoIA1987@gmail.com">Roberto Izquierdo Amo</a>
 * 
 */
public class GameState {

	/** Current game level. */
	private int level;
	/** Current score. */
	private int score;
	/** Lives currently remaining. */
	private int livesRemainingL;
	private int livesRemainingR;
	/** Bullets shot until now. */
	private int bulletsShot;
	/** Ships destroyed until now. */
	private int shipsDestroyed;
	/** Current coin. */
	private int coin;

	/**
	 * Constructor.
	 * 
	 * @param level
	 *            Current game level.
	 * @param score
	 *            Current score.
	 * @param livesRemainingL
	 *            Lives currently remaining Left.
     * @param livesRemainingR
	 * 	          Lives currently remaining Right.
	 * @param bulletsShot
	 *            Bullets shot until now.
	 * @param shipsDestroyed
	 *            Ships destroyed until now.
	 */
	public GameState(final int level, final int score,
			final int livesRemainingL, final int livesRemainingR, final int bulletsShot,
			final int shipsDestroyed, final int coin) {
		this.level = level;
		this.score = score;
		this.livesRemainingL = livesRemainingL;
		this.livesRemainingR = livesRemainingR;
		this.bulletsShot = bulletsShot;
		this.shipsDestroyed = shipsDestroyed;
		this.coin = coin;
	}

	/**
	 * @return the level
	 */
	public final int getLevel() {
		return level;
	}

	/**
	 * @return the score
	 */
	public final int getScore() {
		return score;
	}

	/**
	 * @return the livesRemaining
	 */
	public final int getLivesRemainingL() {
		return livesRemainingL;
	}

	/**
	 * @return the livesRemaining
	 */
	public final int getLivesRemainingR() {
		return livesRemainingR;
	}

	/**
	 * @return the bulletsShot
	 */
	public final int getBulletsShot() {
		return bulletsShot;
	}

	/**
	 * @return the shipsDestroyed
	 */
	public final int getShipsDestroyed() {
		return shipsDestroyed;
	}

	/**
	 * @return the coin
	 */
	public final int getCoin() { return coin;}

}
