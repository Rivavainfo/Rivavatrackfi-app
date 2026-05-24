with open("firestore.rules", "r") as f:
    content = f.read()

content = content.replace('    match /THEDATA/{userId} {',
'''    match /therivavadata/{userId} {
      allow read, write: if request.auth != null && request.auth.uid == userId;
    }

    match /THEDATA/{userId} {''')

with open("firestore.rules", "w") as f:
    f.write(content)
