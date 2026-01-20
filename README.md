Overview
This project is a complete implementation of the classic Pac-Man game in Java. 
Developed as part of the Ex3 Assignment (Object Oriented Programming), the project consists of two main components:

1. A fully functional graphic game engine, managing the game loop, physics, ghost AI, and GUI.
2. A smart algorithm (Ex3Algo) that navigates the Pac-Man agent, collects points, and evades ghosts efficiently.

🧠 The Algorithm (Ex3Algo)
The core of this project is the Ex3Algo class. The Pac-Man agent does not move randomly.
it calculates the optimal move at every frame using a multi-layered strategy.

Key Strategies:
1. Safe Mode (Virtual Walls)
To ensure survival, the algorithm treats dangerous ghosts not just as enemies, but as walls.
In every frame, a virtual copy of the map (SafeMap) is created.
The positions of the ghosts (and their immediate neighbors) are marked as walls (1).
The pathfinding algorithm runs on this "Safe Map." This guarantees that the calculated path never passes through a ghost.

2. Efficient Pathfinding (BFS)
Instead of calculating the distance to every single pellet separately (which is computationally expensive), the algorithm utilizes Breadth-First Search (BFS).
The BFS runs once from the Pac-Man's current position.
It computes the shortest distance to all reachable cells on the board.
The agent then targets the closest reachable pellet (Pink dot).

3. Escape Mode (Plan B)
What happens if the ghosts block all paths to the pellets? (i.e., the BFS on the SafeMap returns no results). In this scenario, the agent switches to Emergency Escape Mode:
It ignores the "Virtual Walls" and looks at the real map.
It evaluates all immediate valid moves.
For each potential move, it calculates the distance to the closest ghost.
Decision: It chooses the move that maximizes the distance from the nearest ghost (Maximin Strategy).

4. Anti-Stuck Mechanism
To prevent the agent from entering an infinite loop (e.g., moving Left-Right-Left-Right continuously), a history buffer is implemented:
The algorithm remembers the last 10 positions.
If the agent visits the exact same tile too frequently within a short window, a "Stuck Flag" is raised.
he agent is then forced to make random valid moves for a few frames to break out of the loop.
The Game Engine (MyGame):
The MyGame class implements the server-side logic and the graphical interface.
Graphics: Built using the StdDraw library, utilizing Double Buffering for smooth, flicker-free animations.
Game Modes:
Manual Mode: Control the Pac-Man using the Arrow Keys.
Auto Mode: The AI (Ex3Algo) takes full control.
Real-time Switching: Press Space to toggle between modes instantly.
Ghost AI:
Ghosts inside the "Ghost House" have specific logic to exit.
Active ghosts use a hybrid behavior: 70% Chasing (shortest path to player) and 30% Random movement.
This balance ensures the game is challenging but fair.

Project Artifacts:
Ex3_2.jar: Runs the solution for Part 2. 
This launches the game directly in Auto Mode to demonstrate the algorithm.
Ex3_3.jar: Runs the solution for Part 3.
This launches the full game with the Start Menu, allowing the user to choose between Manual and Auto modes.
Controls:
Arrow Keys: Move Pac-Man (Manual Mode).
Space Bar: Toggle between Manual and Auto Mode.
'M': Select Manual Mode at the start screen.
'A': Select Auto Mode at the start screen.

The project includes JUnit tests (MyGameTest) ensuring the integrity of the server logic:
-Validation of map boundaries and wall collisions.
-Verification of the BFS pathfinding logic.
-Ghost house logic validation.
