#!/usr/bin/env bash
# Install git hooks for the project

set -e

echo "Installing git hooks..."

# Copy pre-commit hook
cp scripts/pre-commit-hook .git/hooks/pre-commit
chmod +x .git/hooks/pre-commit

echo "Pre-commit hook installed successfully!"
echo "The hook will automatically update README.md with AI contribution statistics on each commit."
