#!/bin/bash

echo "🚀 Lancement de Nutri Coach Pro..."
echo ""
echo "📦 Compilation du projet..."
mvn clean compile

if [ $? -eq 0 ]; then
    echo ""
    echo "✅ Compilation réussie !"
    echo ""
    echo "🎯 Démarrage de l'application JavaFX..."
    mvn javafx:run
else
    echo ""
    echo "❌ Erreur de compilation. Vérifiez les logs ci-dessus."
    exit 1
fi
