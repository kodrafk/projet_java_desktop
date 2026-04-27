import bcrypt

# Générer le hash pour le mot de passe SuperAdmin@2024
password = "SuperAdmin@2024"
salt = bcrypt.gensalt(rounds=10)
hashed = bcrypt.hashpw(password.encode('utf-8'), salt)

print("========================================")
print("  HASH BCRYPT GENERE")
print("========================================")
print(f"Mot de passe: {password}")
print(f"Hash: {hashed.decode('utf-8')}")
print("========================================")
