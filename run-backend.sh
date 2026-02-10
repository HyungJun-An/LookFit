#!/bin/bash

# Load environment variables from .env
if [ -f .env ]; then
  export $(cat .env | xargs)
fi

# Run Spring Boot
cd backend
./gradlew bootRun
