# bridge-bid-tutor-java
Bridge bidding tutor in Java

# Bridge Bidding Tutor

A comprehensive Spring Boot application for learning and practicing bridge bidding, now featuring both traditional server-side rendering and a modern React frontend.

## Features

- **Interactive Bridge Bidding**: Practice bidding with AI opponents
- **Multiple Training Modes**: Single-hand (AI opponents) and multi-hand (manual control)
- **Bidding Systems**: Support for 2/1 Game Forcing, Standard American, Precision, and Acol
- **Real-time Feedback**: Get bidding advice and see hand analysis
- **Deal History**: Review past deals and bidding sequences
- **Modern UI**: Choose between Thymeleaf server-side rendering or React SPA

## Architecture

### Backend (Spring Boot)
- **REST API**: `/api/*` endpoints for React frontend
- **Traditional MVC**: Server-side rendering with Thymeleaf templates
- **Database**: H2 in-memory database with JPA entities
- **Services**: Core bidding logic and AI opponent simulation

### Frontend Options
1. **Thymeleaf Templates**: Traditional server-side rendering (existing)
2. **React SPA**: Modern single-page application with TypeScript

## Quick Start

### Prerequisites
- Java 17 or higher
- Node.js 16+ and npm (for React frontend)
- Gradle (included via wrapper)

### Running with Thymeleaf UI (Traditional)
```bash
./gradlew bootRun
```
Access at: http://localhost:8080

### Running with React UI (Modern)
1. **Start the backend:**
   ```bash
   ./gradlew bootRun
   ```

2. **Install and start React frontend:**
   ```bash
   cd frontend
   npm install
   npm start
   ```
   Access at: http://localhost:3000

### Building for Production
```bash
# This will build React frontend and package it with Spring Boot
./gradlew bootJar

# Run the production JAR
java -jar build/libs/bridge-bid-tutor-java-0.0.1-SNAPSHOT.jar
```

## API Endpoints

### Game State
- `GET /api/game-state` - Get current game state
- `POST /api/new-deal` - Start a new deal
- `POST /api/make-bid` - Make a bid

### History
- `GET /api/past-deals` - Get completed deals
- `GET /api/advice/{handIndex}` - Get bidding advice

## Project Structure

```
src/
├── main/
│   ├── java/com/example/bridge/
│   │   ├── controller/
│   │   │   ├── BridgeBiddingController.java      # Thymeleaf MVC
│   │   │   └── BridgeBiddingRestController.java  # REST API
│   │   ├── model/                                # JPA entities
│   │   ├── service/                              # Business logic
│   │   └── config/                               # Configuration
│   └── resources/
│       ├── templates/                            # Thymeleaf templates
│       └── static/                               # Static resources
├── test/                                         # Unit tests
└── frontend/                                     # React application
    ├── src/
    │   ├── components/                           # React components
    │   ├── types.ts                              # TypeScript interfaces
    │   └── api.ts                                # API client
    └── public/                                   # Static assets
```

## Development

### Running Tests
```bash
./gradlew test
```

### Development Mode
- Backend: `./gradlew bootRun` (auto-restart on changes)
- Frontend: `cd frontend && npm start` (hot reload)

## Training Modes

### Single Hand Mode
- You play as South
- AI handles North, East, and West automatically
- Focus on learning optimal bidding responses

### Multi Hand Mode
- Manual control of all four players
- Practice complete bidding sequences
- Understand bidding from all perspectives

## Bidding Systems

- **2/1 Game Forcing**: Modern standard system
- **Standard American**: Traditional 5-card major system
- **Precision**: Strong club system
- **Acol**: Popular British system

## Technologies Used

### Backend
- Spring Boot 3.2.6
- Spring Data JPA
- H2 Database
- Thymeleaf (optional)
- Java 17+

### Frontend
- React 18
- TypeScript
- Modern CSS with CSS Grid/Flexbox
- Responsive design

## Contributing

1. Fork the repository
2. Create a feature branch
3. Make your changes
4. Run tests: `./gradlew test`
5. Submit a pull request

## License

This project is licensed under the MIT License - see the LICENSE file for details.

## Troubleshooting

### Java 24 Compatibility
If running on Java 24, the project includes fixes for Mockito/Byte Buddy compatibility:
- Uses `mockito-inline` instead of `mockito-core`
- Includes JVM argument `-Dnet.bytebuddy.experimental=true`

### React Build Issues
If you encounter Node.js/npm issues:
1. Ensure Node.js 16+ is installed
2. Clear npm cache: `npm cache clean --force`
3. Delete `node_modules` and run `npm install` again

### Port Conflicts
- Backend runs on port 8080
- React dev server runs on port 3000
- Change ports in `application.properties` or `package.json` if needed
