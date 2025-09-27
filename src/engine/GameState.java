// engine/GameState.java
package engine;

/**
 * Implements an object that stores the state of the game between levels -
 * supports 2-player co-op with shared lives.
 *
 * @author <a href="mailto:RobertoIA1987@gmail.com">Roberto Izquierdo Amo</a>
 *
 * 
 */
public class GameState {

	// 2P mode: number of players used for shared lives in co-op
	public static final int NUM_PLAYERS = 2; // adjust later if needed

	// 2P mode: true if in co-op mode
	private final boolean coop;

	/** Current game level. */
	private int level;

	// 2P mode: if true, lives are shared in a team pool; else per-player lives
	private final boolean sharedLives;

	// team life pool and cap (used when sharedLives == true).
	private int teamLives;
	private int teamLivesCap;

	/** Current coin count. */ // ADD THIS LINE
	private int coins; // ADD THIS LINE

	// 2P mode: co-op aware constructor used by the updated Core loop - livesEach
	// applies per-player; co-op uses shared pool.
	public GameState(final int level, final int livesEach, final boolean coop) {
		this.level = level;
		this.coop = coop;

		if (coop) {
			this.sharedLives = true;
			this.teamLives = Math.max(0, livesEach * NUM_PLAYERS);
			this.teamLivesCap = this.teamLives;
		} else {
			this.sharedLives = false;
			this.teamLives = 0;
			this.teamLivesCap = 0;
			// legacy: put all lives on P1
			lives[0] = Math.max(0, livesEach);
		}
		this.coins = 0; // coins start at 0 by default instead of extra parameter
	}

	// 2P mode: per-player tallies (used for stats/scoring; lives[] unused in shared
	// mode).
	private final int[] score = new int[NUM_PLAYERS];
	private final int[] lives = new int[NUM_PLAYERS];
	private final int[] bulletsShot = new int[NUM_PLAYERS];
	private final int[] shipsDestroyed = new int[NUM_PLAYERS];

	/* ---------- Constructors ---------- */

	/** Legacy 6-arg - kept for old call sites */
	/**
	 * Constructor.
	 *
	 * @param level
	 *                       Current game level.
	 * @param score
	 *                       Current score.
	 * @param livesRemaining
	 *                       Lives currently remaining.
	 * @param bulletsShot
	 *                       Bullets shot until now.
	 * @param shipsDestroyed
	 *                       Ships destroyed until now.
	 * @param coins          // ADD THIS LINE
	 *                       Current coin count. // ADD THIS LINE
	 */
	public GameState(final int level, final int score,
			final int livesRemaining, final int bulletsShot,
			final int shipsDestroyed, final int coins) { // MODIFY THIS LINE
		this.level = level;
		this.sharedLives = false;
		this.teamLives = 0;
		this.teamLivesCap = 0;

		this.score[0] = score;
		this.lives[0] = livesRemaining;
		this.bulletsShot[0] = bulletsShot;
		this.shipsDestroyed[0] = shipsDestroyed;
		this.coins = coins; // ADD THIS LINE
		this.coop = false; // 2P: single-player mode
	}

	/* ------- 2P mode: aggregate totals used by Core/ScoreScreen/UI------- */
	public int getScore() {
		int t = 0;
		for (int p = 0; p < NUM_PLAYERS; p++)
			t += score[p];
		return t;
	}

	public int getLivesRemaining() {
		return sharedLives ? teamLives : (lives[0] + lives[1]);
	}

	public int getBulletsShot() {
		int t = 0;
		for (int p = 0; p < NUM_PLAYERS; p++)
			t += bulletsShot[p];
		return t;
	}

	public int getShipsDestroyed() {
		int t = 0;
		for (int p = 0; p < NUM_PLAYERS; p++)
			t += shipsDestroyed[p];
		return t;

	}

	public int getCoins() {
		return coins;
	}

	/* ----- Per-player getters (needed by Score.java) ----- */
	public int getScore(final int p) {
		return (p >= 0 && p < NUM_PLAYERS) ? score[p] : 0;
	}

	public int getBulletsShot(final int p) {
		return (p >= 0 && p < NUM_PLAYERS) ? bulletsShot[p] : 0;
	}

	public int getShipsDestroyed(final int p) {
		return (p >= 0 && p < NUM_PLAYERS) ? shipsDestroyed[p] : 0;
	}

	public void addScore(final int p, final int delta) {
		score[p] += delta;
	}

	public void incBulletsShot(final int p) {
		bulletsShot[p]++;
	}

	public void incShipsDestroyed(final int p) {
		shipsDestroyed[p]++;
	}

	// ===== Mode / life-pool helpers expected elsewhere =====
	public boolean isCoop() {
		return coop;
	}

	public boolean isSharedLives() {
		return sharedLives;
	}

	public int getTeamLives() {
		return teamLives;
	}

	public void addTeamLife(final int n) {
		if (sharedLives)
			teamLives = Math.min(teamLivesCap, teamLives + Math.max(0, n));
	}

	public void decTeamLife(final int n) {
		if (sharedLives)
			teamLives = Math.max(0, teamLives - Math.max(0, n));
	}

	// map "remaining lives" to team pool in co-op; P1 in 1P legacy
	public void setLivesRemaining(int v) {
		if (sharedLives) {
			teamLives = Math.max(0, Math.min(teamLivesCap, v));
		} else {
			lives[0] = Math.max(0, v);
		}
	}

	public int getLevel() {
		return level;
	}

	public void nextLevel() {
		level++;
	}

	// Team alive if pool > 0 (shared) or any player has lives (separate).
	public boolean teamAlive() {
		return sharedLives ? (teamLives > 0) : (lives[0] > 0 || lives[1] > 0);
	}

}
