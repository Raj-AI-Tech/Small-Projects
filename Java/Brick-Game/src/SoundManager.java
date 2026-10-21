import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.LineUnavailableException;

/**
 * Pure Java low-latency retro arcade audio synthesizer.
 * Pre-synthesizes 8-bit sound effects into pooled memory Clips.
 * Features ZERO latency, polyphonic overlapping playback (no sound queuing or delays),
 * and completely non-blocking audio dispatch.
 */
public class SoundManager {
    private static final int SAMPLE_RATE = 22050;
    private final AudioFormat audioFormat;
    
    private boolean muted = false;
    private boolean audioSupported = true;

    // Polyphonic Clip pools to allow simultaneous, instant playback
    private Clip[] brickHitClips;
    private int brickClipIndex = 0;

    private Clip[] paddleHitClips;
    private int paddleClipIndex = 0;

    private Clip[] wallHitClips;
    private int wallClipIndex = 0;

    private Clip lifeLostClip;
    private Clip levelCompleteClip;
    private Clip gameOverClip;

    public SoundManager() {
        this.audioFormat = new AudioFormat(SAMPLE_RATE, 8, 1, true, false);
        try {
            initClips();
        } catch (Throwable t) {
            // Audio hardware unavailable or disabled
            audioSupported = false;
        }
    }

    /**
     * Pre-synthesizes all sounds into audio Clips once at startup.
     */
    private void initClips() throws Exception {
        // 1. Brick Hit: Pool of 8 clips for rapid-fire multi-brick destruction
        byte[] brickHitData = generateToneData(580, 42, WaveType.SQUARE, 0.45);
        brickHitClips = new Clip[8];
        for (int i = 0; i < brickHitClips.length; i++) {
            brickHitClips[i] = createClipFromBuffer(brickHitData);
        }

        // 2. Paddle Hit: Pool of 4 clips
        byte[] paddleHitData = generateToneData(320, 48, WaveType.TRIANGLE, 0.55);
        paddleHitClips = new Clip[4];
        for (int i = 0; i < paddleHitClips.length; i++) {
            paddleHitClips[i] = createClipFromBuffer(paddleHitData);
        }

        // 3. Wall Hit: Pool of 4 clips
        byte[] wallHitData = generateToneData(220, 32, WaveType.SINE, 0.40);
        wallHitClips = new Clip[4];
        for (int i = 0; i < wallHitClips.length; i++) {
            wallHitClips[i] = createClipFromBuffer(wallHitData);
        }

        // 4. Life Lost: Descending slide tone
        byte[] lifeLostData = generateLifeLostData();
        lifeLostClip = createClipFromBuffer(lifeLostData);

        // 5. Level Complete: Ascending major arpeggio
        byte[] levelCompleteData = generateLevelCompleteData();
        levelCompleteClip = createClipFromBuffer(levelCompleteData);

        // 6. Game Over: Descending sorrowful notes
        byte[] gameOverData = generateGameOverData();
        gameOverClip = createClipFromBuffer(gameOverData);
    }

    private Clip createClipFromBuffer(byte[] buffer) throws LineUnavailableException {
        Clip clip = AudioSystem.getClip();
        clip.open(audioFormat, buffer, 0, buffer.length);
        return clip;
    }

    public void toggleMute() {
        this.muted = !this.muted;
    }

    public boolean isMuted() {
        return muted;
    }

    public void setMuted(boolean muted) {
        this.muted = muted;
    }

    /**
     * Plays brick destruction sound immediately from the round-robin pool.
     * Does NOT wait for previous sounds to finish; overlaps seamlessly.
     */
    public synchronized void playBrickHit() {
        if (!audioSupported || muted || brickHitClips == null) return;
        try {
            Clip clip = brickHitClips[brickClipIndex];
            brickClipIndex = (brickClipIndex + 1) % brickHitClips.length;
            playClipInstant(clip);
        } catch (Throwable ignored) {}
    }

    /**
     * Plays paddle bounce pop instantly without delay.
     */
    public synchronized void playPaddleHit() {
        if (!audioSupported || muted || paddleHitClips == null) return;
        try {
            Clip clip = paddleHitClips[paddleClipIndex];
            paddleClipIndex = (paddleClipIndex + 1) % paddleHitClips.length;
            playClipInstant(clip);
        } catch (Throwable ignored) {}
    }

    /**
     * Plays wall bounce click instantly without delay.
     */
    public synchronized void playWallHit() {
        if (!audioSupported || muted || wallHitClips == null) return;
        try {
            Clip clip = wallHitClips[wallClipIndex];
            wallClipIndex = (wallClipIndex + 1) % wallHitClips.length;
            playClipInstant(clip);
        } catch (Throwable ignored) {}
    }

    /**
     * Plays life lost descending tone.
     */
    public synchronized void playLifeLost() {
        if (!audioSupported || muted || lifeLostClip == null) return;
        playClipInstant(lifeLostClip);
    }

    /**
     * Plays level cleared ascending fanfare.
     */
    public synchronized void playLevelComplete() {
        if (!audioSupported || muted || levelCompleteClip == null) return;
        playClipInstant(levelCompleteClip);
    }

    /**
     * Plays game over melody.
     */
    public synchronized void playGameOver() {
        if (!audioSupported || muted || gameOverClip == null) return;
        playClipInstant(gameOverClip);
    }

    /**
     * Rewinds and triggers non-blocking playback of a clip with zero latency.
     */
    private void playClipInstant(Clip clip) {
        if (clip == null) return;
        try {
            if (clip.isRunning()) {
                clip.stop();
            }
            clip.setFramePosition(0);
            clip.start();
        } catch (Throwable ignored) {}
    }

    // =========================================================================
    // WAVEFORM SYNTHESIS HELPERS
    // =========================================================================

    private enum WaveType {
        SINE, SQUARE, TRIANGLE
    }

    private byte[] generateToneData(int frequencyHz, int durationMs, WaveType waveType, double volume) {
        int totalSamples = (SAMPLE_RATE * durationMs) / 1000;
        byte[] buffer = new byte[totalSamples];

        for (int i = 0; i < totalSamples; i++) {
            double t = (double) i / SAMPLE_RATE;
            double cycle = (t * frequencyHz) % 1.0;
            double raw;

            switch (waveType) {
                case SQUARE:
                    raw = (cycle < 0.5) ? 1.0 : -1.0;
                    break;
                case TRIANGLE:
                    raw = 4.0 * Math.abs(cycle - 0.5) - 1.0;
                    break;
                case SINE:
                default:
                    raw = Math.sin(2.0 * Math.PI * cycle);
                    break;
            }

            // Quick attack, smooth exponential decay envelope
            double progress = (double) i / totalSamples;
            double envelope = Math.exp(-progress * 4.5);

            buffer[i] = (byte) (raw * envelope * volume * 115);
        }
        return buffer;
    }

    private byte[] generateLifeLostData() {
        int durationMs = 300;
        int totalSamples = (SAMPLE_RATE * durationMs) / 1000;
        byte[] buffer = new byte[totalSamples];

        for (int i = 0; i < totalSamples; i++) {
            double progress = (double) i / totalSamples;
            double freq = 440.0 - progress * 260.0; // 440Hz -> 180Hz slide
            double t = (double) i / SAMPLE_RATE;
            double sample = Math.sin(2.0 * Math.PI * freq * t);
            double envelope = (1.0 - progress);
            buffer[i] = (byte) (sample * envelope * 90);
        }
        return buffer;
    }

    private byte[] generateLevelCompleteData() {
        int[] notes = {523, 659, 784, 1046}; // C5, E5, G5, C6
        int noteDurationMs = 80;
        int totalSamples = (SAMPLE_RATE * noteDurationMs * notes.length) / 1000;
        byte[] buffer = new byte[totalSamples];

        int samplesPerNote = (SAMPLE_RATE * noteDurationMs) / 1000;
        for (int n = 0; n < notes.length; n++) {
            int freq = notes[n];
            int offset = n * samplesPerNote;
            for (int i = 0; i < samplesPerNote && (offset + i) < totalSamples; i++) {
                double t = (double) i / SAMPLE_RATE;
                double cycle = (t * freq) % 1.0;
                double raw = (cycle < 0.5) ? 1.0 : -1.0;
                double progress = (double) i / samplesPerNote;
                double envelope = Math.exp(-progress * 2.5);
                buffer[offset + i] = (byte) (raw * envelope * 55);
            }
        }
        return buffer;
    }

    private byte[] generateGameOverData() {
        int[] notes = {392, 349, 311, 261}; // G4, F4, Eb4, C4
        int noteDurationMs = 110;
        int totalSamples = (SAMPLE_RATE * noteDurationMs * notes.length) / 1000;
        byte[] buffer = new byte[totalSamples];

        int samplesPerNote = (SAMPLE_RATE * noteDurationMs) / 1000;
        for (int n = 0; n < notes.length; n++) {
            int freq = notes[n];
            int offset = n * samplesPerNote;
            for (int i = 0; i < samplesPerNote && (offset + i) < totalSamples; i++) {
                double t = (double) i / SAMPLE_RATE;
                double cycle = (t * freq) % 1.0;
                double raw = 4.0 * Math.abs(cycle - 0.5) - 1.0; // Triangle wave
                double progress = (double) i / samplesPerNote;
                double envelope = Math.exp(-progress * 2.2);
                buffer[offset + i] = (byte) (raw * envelope * 65);
            }
        }
        return buffer;
    }
}
