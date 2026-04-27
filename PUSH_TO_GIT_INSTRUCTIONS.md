# Push to Git - integration_java Branch

## I've initialized the Git repository for you!

Now follow these steps to push to the `integration_java` branch:

## Step-by-Step Instructions

### Step 1: Add Your Remote Repository

Replace `YOUR_REPO_URL` with your actual GitHub repository URL:

```bash
git remote add origin YOUR_REPO_URL
```

**Example:**
```bash
git remote add origin https://github.com/yourusername/nutrilife.git
```

Or if you already have a remote:
```bash
git remote -v
```

### Step 2: Create and Switch to integration_java Branch

```bash
git checkout -b integration_java
```

### Step 3: Add All Files

```bash
git add .
```

### Step 4: Check What Will Be Committed

```bash
git status
```

You should see:
- ✅ All `.java` files
- ✅ `database/` folder with schema
- ✅ `pom.xml`
- ❌ NOT `target/` (ignored)
- ❌ NOT `uploads/` (ignored)

### Step 5: Commit Your Changes

```bash
git commit -m "Add complaint system with AI emotion analysis features"
```

### Step 6: Push to Remote

```bash
git push -u origin integration_java
```

## Quick Commands (Copy-Paste)

```bash
# 1. Add remote (replace with your URL)
git remote add origin YOUR_GITHUB_REPO_URL

# 2. Create and switch to branch
git checkout -b integration_java

# 3. Add all files
git add .

# 4. Commit
git commit -m "Add complaint system with AI emotion analysis features"

# 5. Push
git push -u origin integration_java
```

## Alternative: Use the Script

I created a script for you. Just run:

**On Windows:**
```bash
./git_push.bat
```

**On Mac/Linux:**
```bash
chmod +x git_push.sh
./git_push.sh
```

## What Gets Pushed

### ✅ Included:
- All source code (`.java`, `.fxml`)
- Database schema (`database/schema.sql`)
- Maven config (`pom.xml`)
- Documentation (`.md` files)
- `.gitignore`

### ❌ Excluded (by .gitignore):
- `target/` - Compiled files
- `uploads/` - User uploads
- `.idea/` - IDE files
- Backup files

## Troubleshooting

### Error: "remote origin already exists"
```bash
git remote remove origin
git remote add origin YOUR_REPO_URL
```

### Error: "Permission denied"
You need to authenticate with GitHub:
- Use HTTPS with personal access token
- Or set up SSH keys

### Error: "Branch already exists"
```bash
git checkout integration_java
git add .
git commit -m "Update complaint system"
git push
```

## Verify Your Push

After pushing, check on GitHub:
1. Go to your repository
2. Switch to `integration_java` branch
3. You should see all your files

## Important Notes

⚠️ **API Key Warning:**
Your API key is in the code (`GeminiService.java`). Anyone who clones the repo can see it.

For production, consider:
- Using environment variables
- Using a config file (not committed)
- Getting a new key for each developer

## Need Your Repository URL?

If you don't have a GitHub repository yet:

1. Go to https://github.com
2. Click "New repository"
3. Name it (e.g., "nutrilife-complaint-system")
4. Copy the repository URL
5. Use it in the `git remote add origin` command

## Summary

```bash
# Quick setup
git remote add origin YOUR_REPO_URL
git checkout -b integration_java
git add .
git commit -m "Add complaint system with AI features"
git push -u origin integration_java
```

Done! Your code is now on GitHub in the `integration_java` branch! 🚀
