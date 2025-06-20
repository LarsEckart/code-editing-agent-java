#!/usr/bin/env bash
# filepath: analyze_ai_contributions.sh

set -e

# Check if we're running bash version 4.0 or higher for associative arrays
if [ "${BASH_VERSION%%.*}" -lt 4 ]; then
    echo "Error: This script requires bash 4.0 or higher for associative arrays."
    echo "Current bash version: $BASH_VERSION"
    exit 1
fi

# Initialize counters
total_commits=0
ai_commits=0
total_lines_added=0
total_lines_deleted=0
ai_lines_added=0
ai_lines_deleted=0

# Declare associative arrays for AI tracking
declare -A ai_commit_counts
declare -A ai_lines_added_counts
declare -A ai_lines_deleted_counts

# Process each commit
while IFS= read -r commit_hash; do
    total_commits=$((total_commits + 1))
    
    # Get commit message and check for AI co-author
    commit_message=$(git log -1 --pretty=format:"%B" "$commit_hash")
    
    # Check if commit has AI co-author and identify which AI
    is_ai_commit=0
    ai_name=""
    
    if echo "$commit_message" | grep -iE 'Co-authored-by:.*Claude' > /dev/null; then
        ai_name="Claude Code"
        is_ai_commit=1
    elif echo "$commit_message" | grep -iE 'Co-authored-by:.*Copilot' > /dev/null; then
        ai_name="GitHub Copilot"
        is_ai_commit=1
    elif echo "$commit_message" | grep -iE 'Co-authored-by:.*ChatGPT' > /dev/null; then
        ai_name="ChatGPT"
        is_ai_commit=1
    elif echo "$commit_message" | grep -iE 'Co-authored-by:.*Amp' > /dev/null; then
        ai_name="Amp"
        is_ai_commit=1
    elif echo "$commit_message" | grep -iE 'Co-authored-by:.*AI' > /dev/null; then
        ai_name="AI (Generic)"
        is_ai_commit=1
    fi
    
    if [ "$is_ai_commit" -eq 1 ]; then
        ai_commits=$((ai_commits + 1))
        ai_commit_counts["$ai_name"]=$((ai_commit_counts["$ai_name"] + 1))
    fi
    
    # Get stats for this commit
    commit_lines_added=0
    commit_lines_deleted=0
    
    while IFS=$'\t' read -r added deleted file; do
        # Skip non-numeric lines and binary files
        if [[ "$added" =~ ^[0-9]+$ && "$deleted" =~ ^[0-9]+$ ]]; then
            total_lines_added=$((total_lines_added + added))
            total_lines_deleted=$((total_lines_deleted + deleted))
            commit_lines_added=$((commit_lines_added + added))
            commit_lines_deleted=$((commit_lines_deleted + deleted))
            
            if [ "$is_ai_commit" -eq 1 ]; then
                ai_lines_added=$((ai_lines_added + added))
                ai_lines_deleted=$((ai_lines_deleted + deleted))
                ai_lines_added_counts["$ai_name"]=$((ai_lines_added_counts["$ai_name"] + added))
                ai_lines_deleted_counts["$ai_name"]=$((ai_lines_deleted_counts["$ai_name"] + deleted))
            fi
        fi
    done < <(git show --numstat --format=format: "$commit_hash")
    
done < <(git log --pretty=format:"%H")

# Calculate percentages
ai_commit_percentage=$(awk "BEGIN { printf \"%.2f\", ($ai_commits / $total_commits) * 100 }")
ai_lines_added_percentage=$(awk "BEGIN { printf \"%.2f\", ($ai_lines_added / $total_lines_added) * 100 }")
ai_lines_changed_percentage=$(awk "BEGIN { printf \"%.2f\", ($ai_lines_added + $ai_lines_deleted) / ($total_lines_added + $total_lines_deleted) * 100 }")

# Print report in markdown format
echo "### Project Statistics"
echo ""
echo "- **Total Commits**: $total_commits"
echo "- **AI-Assisted Commits**: $ai_commits ($ai_commit_percentage%)"
echo "- **Total Lines Added**: $total_lines_added"
echo "- **AI-Assisted Lines Added**: $ai_lines_added ($ai_lines_added_percentage%)"
echo "- **Total Lines Changed**: $((total_lines_added + total_lines_deleted))"
echo "- **AI-Assisted Lines Changed**: $((ai_lines_added + ai_lines_deleted)) ($ai_lines_changed_percentage%)"

# Print AI breakdown
if [ ${#ai_commit_counts[@]} -gt 0 ]; then
    echo ""
    echo "### Breakdown by AI Assistant"
    echo ""
    # Define the fixed order of AI assistants
    ai_order=("Claude Code" "Amp" "GitHub Copilot" "ChatGPT" "AI (Generic)")
    for ai in "${ai_order[@]}"; do
        if [ -n "${ai_commit_counts[$ai]+set}" ]; then
            ai_commits_for_this_ai=${ai_commit_counts[$ai]}
            ai_lines_added_for_this_ai=${ai_lines_added_counts[$ai]}
            ai_lines_deleted_for_this_ai=${ai_lines_deleted_counts[$ai]}
            ai_lines_changed_for_this_ai=$((ai_lines_added_for_this_ai + ai_lines_deleted_for_this_ai))

            ai_commit_percentage_for_this_ai=$(awk "BEGIN { printf \"%.2f\", ($ai_commits_for_this_ai / $total_commits) * 100 }")
            ai_lines_percentage_for_this_ai=$(awk "BEGIN { printf \"%.2f\", ($ai_lines_changed_for_this_ai / ($total_lines_added + $total_lines_deleted)) * 100 }")

            echo "#### $ai"
            echo ""
            echo "- **Commits**: $ai_commits_for_this_ai ($ai_commit_percentage_for_this_ai%)"
            echo "- **Lines Added**: $ai_lines_added_for_this_ai"
            echo "- **Lines Deleted**: $ai_lines_deleted_for_this_ai"
            echo "- **Lines Changed**: $ai_lines_changed_for_this_ai ($ai_lines_percentage_for_this_ai%)"
            echo ""
        fi
    done
fi
