/**
 * Represents the distinct states of the Brick Breaker game.
 */
public enum GameState {
    /** Start / title screen displaying instructions. */
    START,
    
    /** Ball is resting on the paddle waiting for player launch. */
    BALL_READY,
    
    /** Active gameplay where ball is in motion. */
    PLAYING,
    
    /** Game is paused by player. */
    PAUSED,
    
    /** Level has been cleared; displaying level transition banner. */
    LEVEL_COMPLETE,
    
    /** Player lost all lives; displaying Game Over screen. */
    GAME_OVER,
    
    /** Player beat the final campaign level (with option to continue infinite mode). */
    WIN
}
