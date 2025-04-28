# Gone Fishing - LibGDX Fishing Game

A simple fishing clicker game made with LibGDX.

## Game Overview

Gone Fishing is a simple fishing clicker game where players:
1. Click the yellow circle button to cast their fishing line
2. Wait for the line to extend fully
3. Click again to reel in the line
4. Repeat to catch fish!

## Game Controls

- Click the yellow circle button at the bottom to cast your fishing line
- When the line is fully extended, click anywhere to reel it in
- The fishing rod is represented by a simple orange rectangle
- The fishing line is a white line with a red circle at the end (the bait)

## Project Structure

The project follows SOLID principles and uses several design patterns:
- **Model-View-Controller**: Separates game logic from rendering
- **Component-Based Design**: Entities have specific behaviors and renderers
- **Singleton**: Used for resource management

### Key Classes

- `GoneFishingGame`: Main game class extending LibGDX's Game class
- `GameScreen`: Handles rendering and game state
- `FishingRod`: Manages fishing rod logic and rendering
- `InputHandler`: Processes user input
- `ShapeRendererManager`: Utility class to manage ShapeRenderer lifecycle

## Running the Game

### From IDE
1. Open the project in your IDE
2. Run the `Lwjgl3Launcher` class

### From Command Line
1. Navigate to the project directory
2. Run `./gradlew lwjgl3:run`

## Future Enhancements

- Add fish that appear randomly
- Implement a scoring system
- Add different fishing rods and baits
- Create a shop to buy upgrades

## Credits

Created with LibGDX - A Java game development framework
