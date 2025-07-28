import csv
import random

# Define FPS and run duration
FPS = 60
DURATION_SECONDS = 300  # 5 minutes

# Define shape templates as (xOffset, yOffset)
SHAPES = {
    "diamond":        [(0,0), (-50,50), (50,50), (-75,100), (75,100), (-50,150), (50,150), (0,200)],
    "grid_2x3":       [(-50,0), (0,0), (50,0), (-50,100), (0,100), (50,100)],
    "t_shape":        [(-50,0), (0,0), (50,0), (0,100), (0,200), (-50,100), (50,100)],
    "y_shape":        [(-100,0), (0,0), (100,0), (-50,50), (50,50), (0,100), (0,150)],
    "arrow_down":     [(0,0), (-25,25), (25,25), (0,75), (0,100), (0,150)],
    "vertical_line":  [(0,i*50) for i in range(5)],
    "horizontal_line":[(i*50, 0) for i in range(-2, 3)],
    "x_shape":        [(-100,0), (100,0), (-50,50), (50,50), (0,100), (-50,150), (50,150), (-100,200), (100,200)],
}

# Define enemy types for randomization
ENEMY_TYPES = ["alien1", "shielded"]

# Define power-up types
POWER_UP_TYPES = ["multi", "speed", "bomb"]

# Compute interval and number of waves
INTERVAL_FRAMES = 500
NUM_WAVES = int(FPS * DURATION_SECONDS / INTERVAL_FRAMES)

# Center for shapes
BASE_X = 250

# Random power-up insertion settings
MIN_WAVES_BETWEEN_PU = 1
MAX_WAVES_BETWEEN_PU = 3

# Random double-wave settings
MIN_WAVES_BEFORE_DOUBLE = 2
MAX_WAVES_BEFORE_DOUBLE = 4
waves_since_double = 0
next_double = random.randint(MIN_WAVES_BEFORE_DOUBLE, MAX_WAVES_BEFORE_DOUBLE)

output_file = "spawn_scene2.csv"
rows = [["frame", "type", "x", "yOffset"]]

# Track waves since last power-up and next threshold
waves_since_pu = 0
next_pu_threshold = random.randint(MIN_WAVES_BETWEEN_PU, MAX_WAVES_BETWEEN_PU)

for wave_idx in range(NUM_WAVES):
    frame = wave_idx * INTERVAL_FRAMES
    waves_since_double += 1

    def spawn_shape(shape, x_center):
        # Randomize enemy type per shape instance
        for x_off, y_off in SHAPES[shape]:
            enemy_type = random.choice(ENEMY_TYPES)
            rows.append([frame, enemy_type, x_center + x_off, y_off])

    if waves_since_double >= next_double:
        # Double wave: two shapes offset left/right
        shape1 = random.choice(list(SHAPES.keys()))
        shape2 = random.choice(list(SHAPES.keys()))
        spawn_shape(shape1, BASE_X - 100)
        spawn_shape(shape2, BASE_X + 100)
        waves_since_double = 0
        next_double = random.randint(MIN_WAVES_BEFORE_DOUBLE, MAX_WAVES_BEFORE_DOUBLE)
    else:
        # Single wave
        shape = random.choice(list(SHAPES.keys()))
        spawn_shape(shape, BASE_X)

    # Power-ups logic
    waves_since_pu += 1
    if waves_since_pu >= next_pu_threshold:
        pu_count = random.choice([1, 2])
        for _ in range(pu_count):
            pu_type = random.choice(POWER_UP_TYPES)
            pu_x = BASE_X + random.randint(-200, 200)
            rows.append([frame, pu_type, pu_x, 0])
        waves_since_pu = 0
        next_pu_threshold = random.randint(MIN_WAVES_BETWEEN_PU, MAX_WAVES_BETWEEN_PU)

# Boss and guaranteed power-ups at end
boss_frame = NUM_WAVES * INTERVAL_FRAMES + INTERVAL_FRAMES
rows.append([boss_frame, "multi", BASE_X - 100, 0])
rows.append([boss_frame, "speed", BASE_X + 100, 0])
rows.append([boss_frame, "largealien", BASE_X, 0])

# Write out CSV
with open(output_file, "w", newline="") as f:
    writer = csv.writer(f)
    writer.writerows(rows)

print(f"Generated {NUM_WAVES} waves with random enemy types (alien1/shielded), occasional double waves, power-ups, and boss into '{output_file}'.")
