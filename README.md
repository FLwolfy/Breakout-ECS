# breakout
## NAME Hsuan-Kai Liao

This project implements the game of Breakout with multiple levels.

### Timeline

 * Start Date: 01/13/25

 * Finish Date: 01/21/25

 * Hours Spent: Around 50hrs



### Attributions

 * Resources used for learning (including AI assistance):
   * Knowledge about Game Engine (Especially the ECS system) -- https://thecherno.com/engine 
   * Script-like programming language inspired by Unity Engine -- https://docs.unity3d.com/Manual/ScriptingSection.html
   * Collider and Physics System -- https://learnopengl.com/In-Practice/2D-Game/Collisions/Collision-detection
   * Copilot by GitHub -- https://copilot.github.com/
   * ChatGPT by OpenAI -- https://beta.openai.com/
 
 * Resources used directly (including AI assistance)
   * Duke Logo Image -- https://www.pngkit.com/view/u2q8w7y3w7t4e6u2_duke-university-seal-duke-university-seal-logo/
   * Copilot for generating IO functions
   * ChatGPT for helping the comments

### Running the Program

 * Main class:
   * Engine Part: GameObject, GameScene, GameComponent, Collider, Transform, RenderHandler 
   * Game Part: Bouncer, Paddle, Brick, Power, MainScene

 * Data files needed:
   * Image: Duke Logo Image
   * Levels: all the .level files inside the level folder

 * Key/Mouse inputs: 
   * Arrow keys: Move the paddle
   * Mouse Primary Button: Select and interact with the UI
   * P: Pause the game
   * S: Restart the level
   * 0-9: Load the level 0-3 (larger than 3 will load the last level, you can add more levels in the level folder)

 * Cheat keys: (Combined with the key above are all input keys)
   * L: Get extra life
   * R: Reset the bouncer and paddle position without resetting the level

### Notes/Assumptions

 * Assumptions or Simplifications:

 * Known Bugs: Due to the fact that this is my first time to implement the ECS and directly use it inside the BreakOut Game implementation, there might be some sequential update bugs that may lead to some resets not working. However, I have tested the game multiple times and it works fine most of the time. If I have time, I would try to refactor the code in the ECS system to make it more robust (like the update sequence and action subscription handling).

 * [Features](https://courses.cs.duke.edu/compsci308/spring25/assign/01_game/requirements.php) implemented: 
   * All the CORES and EXTENTIONS (BREAK 1-15) are implemented.
   * Paddle Behavior:
     * The paddle can be bounce back the bouncer with a random angle offset.
     * The paddle can be warped if it cross the edges of the screen.
     * The paddle's horizontal velocity will be added up to the bouncer (with multiplier) to make the bouncer somewhat controllable by the player.
   * Special Block Type:
     * Multi Hit Block: The block will take multiple hits to be destroyed.
     * Explosive Block: The block will explode and damage the surrounding blocks when it is destroyed.
     * Laser Block: The block will shoot a horizontal laser that will damage the blocks in the same row.
   * Power Up:
     * Extra Life: The player will get an extra life within the duration. (after the duration, the life will be reset. Like the [Golden Apple](https://en.wikipedia.org/wiki/Golden_apple) in *Minecraft*)
     * Longer Paddle: The paddle will be enlarged within the duration.
     * Speed Up: The bouncer will move faster within the duration.
     * Icy Paddle: The paddle will be slippery (no velocity decrease) within the duration.
   * Cheat Keys (See **Key/Mouse inputs** above)

 * Features unimplemented (in the original [plan](doc/PLAN.md)):
   * *Interesting Breakout Variants:* Idea #1 ***[Puzzle Bobble](https://en.wikipedia.org/wiki/Puzzle_Bobble)* Mode** and Idea #2 **Moving Wall Mode**
   * *Block Ideas:* Idea #4: **Guardian Blocks**
   * *Power-up Ideas:* Idea #1: **Extra Balls** and Idea #3: **Good Night**
   * *Cheat Key Ideas:* All Ideas have been changed to the current cheat keys
   * *Level Descriptions:* Idea #2: **Naughty Walls**

 * Noteworthy Features: All the collisions within the game are **NOT** using simple ifelse statements, but using the ECS system to handle the collision detection and resolution. This makes it really easy to consider situations where there are some more fun and complex effect (with physics considerations for instance. BTW, **DO YOU CLICK THE DUKE LOGO IN THE MENU?**). Also, the game has multiple levels and the level can be easily added by adding a new .level file in the level folder.

### Assignment Impressions

At the very first, I didn't notice there is a full list of requirements, so my plan is somehow really different from the course required one. That is the main reason why I didn't manage to complete a lot of features in my original plan. However, I think the current implementation is really fun and I have learned a lot from the project. I have learned how to use the ECS system to handle the game logic and how to make the game more fun by adding more features. I think the most challenging part is to make the game more robust and make the ECS system more flexible. I think I have done a good job in the end, but there are still some bugs that I didn't manage to fix. I think I will try to fix them in the future if I have time. I think the most rewarding part is to see the game running and the player enjoying the game. I think I have done a good job in the end, but there are still some bugs that I didn't manage to fix. I think I will try to fix them in the future if I have time. The most important part, and also the most time-consuming part is the collision detection part. I have spent tons of hours to debug and to learn the Math behind it. I'm very glad that it actually works, especially when you click on the Duke Logo, there will pop out 15 balls and all of them will have physics impact on each other. Overall, I have spent many efforts on this project, and I also learnt a lot during the process. Due to the time limit, This is the best I can make. I would definitely polish my code for the ECS system as I learn more "Good Programming Skill and Habits" in future class.