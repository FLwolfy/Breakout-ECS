# Breakout Plan
### NAME: Hsuan-Kai Liao

## Interesting Breakout Variants

 * Idea #1: ***[Puzzle Bobble](https://en.wikipedia.org/wiki/Puzzle_Bobble)* Mode**: In this mode, the "ceiling" of the playing arena drops downwards slightly every several seconds, along with all the blocks stuck to it, just like the game *Puzzle Bobble*. Players have to eliminate the blocks as soon as possible, or once the blocks reach the bottom, they lose.

 * Idea #2: **Moving Wall Mode**: In this mode, the walls will be moving, so the movement of the balls will be more random.


## Paddle Ideas

 * Idea #1: **Friction Included**: The paddle will have friction included, which means when you move left the paddle during the hit, the ball will bounce more towards the left.

 * Idea #2: **Wrapping**: The paddle will warp from one side of the screen to the other when it reaches the edge.


## Block Ideas

 * Idea #1: **Block with Lives**: Some blocks require multiple hits to be destroyed.

 * Idea #2: **Explosive Blocks**: Some blocks may explode after it get destroyed, and will damage the surrounding blocks.

 * Idea #3: **Lazer Blocks**: Some blocks will shoot a lazer out after it get destroyed, and will damage the whole line of blocks that the lazer covers.

 * Idea #4: **Guardian Blocks**: Some edges of certain blocks are reinforced and cannot be hit. Players must attack from the unguarded side or think of other method.


## Power-up Ideas

 * Idea #1: **Extra Balls**: This power-up will generate more balls out.

 * Idea #2: **Stretch the Paddle**: This power-up will randomly stretch the paddle within a period of time (shorten or lengthen, who knows).

 * Idea #3: **Good Night**: This power-up will turn everything dark and hard to be seen.


## Cheat Key Ideas

 * Idea #1: **Q Key**: This will generate more balls for the player.

 * Idea #2: **W Key**: This will make the paddle infinite long.

 * Idea #3: **E Key**: This will freeze the scene but you can still control the paddle.

 * Idea #4: **R Key**: This will give players extra lives.


## Level Descriptions

 * Idea #1: **The King and the Betrayers**: A block is surrounded with concrete defence. Players cannot directly damage it with balls, but maybe a lazer block will help.

 * Idea #2: **Naughty Walls**: Players have to clear all the blocks with the walls moving.


## Class Ideas

 * Idea #1: **Block Class**: This is the fundamental class to represent the object block. It should have methods like `GetDamage()` and `OnDestroy()`.

 * Idea #2: **Bounce Ball Class**: This is the class of the bounce ball. It should have methods like `SetVelocity(double x, double y)` and `GetVelocity()`.

 * Idea #3: **Paddle Class**: This should be the class of the paddle. It should contain methods like `MoveLeft()` and `GetVelocity()`.

 * Idea #4: **GameScene Class**: This is the class representing to show every elements mentioned above in the game. It also somewhat links the inputs with the corresponding controls of paddles or cheat keys.

