#!/bin/bash
# filepath: analyze_ai_contributions.sh

set -e

echo "Analyzing git commit history for AI contributions..."

# Initialize counters
total_commits=0
ai_commits=0
total_lines_added=0
total_lines_deleted=0
ai_lines_added=0
ai_lines_deleted=0

# Process each commit
while IFS= read -r commit_hash; do
    total_commits=$((total_commits + 1))
    
    # Check if commit has AI co-author (matches common AI assistant names)
    is_ai_commit=0
    if git log -1 --pretty=format:"%B" "$commit_hash" | grep -iE 'Co-authored-by:.*(AI|Copilot|Amp|Anthropic|OpenAI|ChatGPT|Claude)' > /dev/null; then
        ai_commits=$((ai_commits + 1))
        is_ai_commit=1
    fi
    
    # Get stats for this commit
    while IFS=$'\t' read -r added deleted file; do
        # Skip non-numeric lines and binary files
        if [[ "$added" =~ ^[0-9]+$ && "$deleted" =~ ^[0-9]+$ ]]; then
            total_lines_added=$((total_lines_added + added))
            total_lines_deleted=$((total_lines_deleted + deleted))
            
            if [ "$is_ai_commit" -eq 1 ]; then
                ai_lines_added=$((ai_lines_added + added))
                ai_lines_deleted=$((ai_lines_deleted + deleted))
            fi
        fi
    done < <(git show --numstat --format=format: "$commit_hash")
    
done < <(git log --pretty=format:"%H")

# Calculate percentages
ai_commit_percentage=$(awk "BEGIN { printf \"%.2f\", ($ai_commits / $total_commits) * 100 }")
ai_lines_added_percentage=$(awk "BEGIN { printf \"%.2f\", ($ai_lines_added / $total_lines_added) * 100 }")
ai_lines_changed_percentage=$(awk "BEGIN { printf \"%.2f\", ($ai_lines_added + $ai_lines_deleted) / ($total_lines_added + $total_lines_deleted) * 100 }")

# Print report
echo "===== AI Contribution Report ====="
echo "Total commits: $total_commits"
echo "AI-assisted commits: $ai_commits ($ai_commit_percentage%)"
echo ""
echo "Total lines added: $total_lines_added"
echo "AI-assisted lines added: $ai_lines_added ($ai_lines_added_percentage%)"
echo ""
echo "Total lines changed (added + deleted): $((total_lines_added + total_lines_deleted))"
echo "AI-assisted lines changed: $((ai_lines_added + ai_lines_deleted)) ($ai_lines_changed_percentage%)"
echo "=================================="
