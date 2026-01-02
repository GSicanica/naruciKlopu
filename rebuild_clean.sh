#!/bin/bash

echo "🧹 Cleaning Android build..."
./gradlew clean

echo "🗑️  Removing build directories..."
find . -type d -name "build" -exec rm -rf {} + 2>/dev/null || true

echo "🗑️  Clearing Gradle cache..."
rm -rf ~/.gradle/caches/

echo "✅ Clean complete!"
echo ""
echo "🔨 Now build the app with:"
echo "./gradlew assembleDebug"
echo ""
echo "Or in Android Studio:"
echo "Build > Rebuild Project"
