#!/bin/bash

# Development server script for Spring Boot application with hot reloading
# Usage: ./dev-server.sh [start|stop|restart|status]

PID_FILE=".dev-server.pid"
LOG_FILE="logs/application-web.log"

start_server() {
  if [ -f "$PID_FILE" ]; then
    PID=$(cat "$PID_FILE")
    if ps -p "$PID" > /dev/null 2>&1; then
      echo "Development server is already running (PID: $PID)"
      return 1
    else
      rm -f "$PID_FILE"
    fi
  fi

  echo "Starting development server with hot reloading..."
  echo "Server will be available at: http://localhost:8080"
  echo "LiveReload available at: http://localhost:35729"
  echo "Logs are written to: $LOG_FILE"
  
  # Start the server in background with development profile
  nohup ./gradlew run -Dapp.mode=web -Dspring.profiles.active=dev > "$LOG_FILE" 2>&1 &
  SERVER_PID=$!
  echo $SERVER_PID > "$PID_FILE"
  
  echo "Development server started (PID: $SERVER_PID)"
  echo "Use './dev-server.sh stop' to stop the server"
  echo "Use 'tail -f $LOG_FILE' to follow the logs"
}

stop_server() {
  if [ ! -f "$PID_FILE" ]; then
    echo "No PID file found. Server may not be running."
    return 1
  fi

  PID=$(cat "$PID_FILE")
  if ps -p "$PID" > /dev/null 2>&1; then
    echo "Stopping development server (PID: $PID)..."
    kill "$PID"
    
    # Wait for process to stop
    for i in {1..10}; do
      if ! ps -p "$PID" > /dev/null 2>&1; then
        break
      fi
      sleep 1
    done
    
    if ps -p "$PID" > /dev/null 2>&1; then
      echo "Force killing server..."
      kill -9 "$PID"
    fi
    
    rm -f "$PID_FILE"
    echo "Development server stopped"
  else
    echo "Server process not found (PID: $PID)"
    rm -f "$PID_FILE"
  fi
}

server_status() {
  if [ -f "$PID_FILE" ]; then
    PID=$(cat "$PID_FILE")
    if ps -p "$PID" > /dev/null 2>&1; then
      echo "Development server is running (PID: $PID)"
      echo "Server URL: http://localhost:8080"
      echo "LiveReload URL: http://localhost:35729"
    else
      echo "PID file exists but server is not running"
      rm -f "$PID_FILE"
    fi
  else
    echo "Development server is not running"
  fi
}

restart_server() {
  echo "Restarting development server..."
  stop_server
  sleep 2
  start_server
}

case "${1:-start}" in
  start)
    start_server
    ;;
  stop)
    stop_server
    ;;
  restart)
    restart_server
    ;;
  status)
    server_status
    ;;
  logs)
    if [ -f "$LOG_FILE" ]; then
      tail -f "$LOG_FILE"
    else
      echo "No log file found. Server may not be running."
    fi
    ;;
  *)
    echo "Usage: $0 {start|stop|restart|status|logs}"
    echo ""
    echo "Commands:"
    echo "  start   - Start the development server with hot reloading"
    echo "  stop    - Stop the development server"
    echo "  restart - Restart the development server"
    echo "  status  - Show server status"
    echo "  logs    - Follow server logs"
    echo ""
    echo "Features:"
    echo "  - Automatic restart when Java classes change"
    echo "  - Live reload for static resources and templates"
    echo "  - Development-optimized configuration"
    echo "  - Background process management"
    exit 1
    ;;
esac