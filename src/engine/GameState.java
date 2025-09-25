// engine/GameState.java
package engine;

/**
 * Stores game state between levels. Supports 2-player co-op with shared lives.
 */
public class GameState {

	public static final int NUM_PLAYERS = 2;

	/** Current game level. */
	private int level;

	/** Co-op flag. */
	private final boolean coop;

	/** Shared-lives mode for co-op (team life pool). */
	private final boolean sharedLives;

	/** Team life pool and cap (used when sharedLives = true). */
	private int teamLives;
	private int teamLivesCap;

	/** Per-player tallies (still used for stats/scoring; lives[] unused in shared mode). */
	private final int[] score          = new int[NUM_PLAYERS];
	private final int[] lives          = new int[NUM_PLAYERS];
	private final int[] bulletsShot    = new int[NUM_PLAYERS];
	private final int[] shipsDestroyed = new int[NUM_PLAYERS];

	/* ---------- Constructors ---------- */

	/** Legacy single-player constructor (kept for compatibility). */
	public GameState(final int level, final int score,
					 final int livesRemaining, final int bulletsShot,
					 final int shipsDestroyed) {
		this.level = level;
		this.coop = false;
		this.sharedLives = false;
		this.score[0] = score;
		this.lives[0] = livesRemaining;
		this.bulletsShot[0] = bulletsShot;
		this.shipsDestroyed[0] = shipsDestroyed;
	}

	/**
	 * New co-op constructor with SHARED lives.
	 * @param level starting level
	 * @param livesEach lives per player (used to compute team pool = livesEach * NUM_PLAYERS)
	 * @param coop set true for co-op
	 */
	public GameState(final int level, final int livesEach, final boolean coop) {
		this.level = level;
		this.coop = coop;
		this.sharedLives = true;                    // shared pool ON
		this.teamLives = livesEach * NUM_PLAYERS;   // e.g., 3 * 2 = 6 total hearts
		this.teamLivesCap = livesEach * NUM_PLAYERS;
		// per-player lives[] unused in shared mode, but left at 0
	}

	/* ---------- Per-player stats API ---------- */
	public int  getScore(final int p)          { return score[p]; }
	public int  getLives(final int p)          { return sharedLives ? teamLives : lives[p]; }
	public int  getBulletsShot(final int p)    { return bulletsShot[p]; }
	public int  getShipsDestroyed(final int p) { return shipsDestroyed[p]; }

	public void addScore(final int p, final int delta) { score[p] += delta; }
	public void incBulletsShot(final int p)            { bulletsShot[p]++; }
	public void incShipsDestroyed(final int p)         { shipsDestroyed[p]++; }

	/** Decrement life (shared pool if enabled; otherwise per player). */
	public void decLife(final int p) {
		if (sharedLives) decTeamLife(1);
		else if (lives[p] > 0) lives[p]--;
	}

	/* ---------- Shared-lives helpers ---------- */
	public boolean isSharedLives()         { return sharedLives; }
	public int     getTeamLives()          { return teamLives; }
	public void    addTeamLife(final int n){ teamLives = Math.min(teamLivesCap, teamLives + n); }
	public void    decTeamLife(final int n){ teamLives = Math.max(0, teamLives - n); }

	/* ---------- Aggregate (legacy) getters: keep UI working ---------- */
	public final int getScore()          { return score[0] + score[1]; }
	public final int getLivesRemaining() { return sharedLives ? teamLives : (lives[0] + lives[1]); }
	public final int getBulletsShot()    { return bulletsShot[0] + bulletsShot[1]; }
	public final int getShipsDestroyed() { return shipsDestroyed[0] + shipsDestroyed[1]; }

	/* ---------- Level & mode ---------- */
	public final int getLevel() { return level; }
	public void nextLevel()     { level++; }
	public boolean isCoop()     { return coop; }

	/** Team is alive while teamLives > 0 (shared) or any player has lives (separate). */
	public boolean teamAlive()  { return sharedLives ? teamLives > 0 : (lives[0] > 0 || lives[1] > 0); }
}
