#!/bin/bash

# Test script for Gemini provider
# Usage: ./test-gemini.sh
# Make sure to set GOOGLE_API_KEY environment variable

export AI_PROVIDER=gemini

echo "Testing with Gemini provider..."
echo "Make sure you have GOOGLE_API_KEY set in your environment"
echo ""

./cli-app.sh
