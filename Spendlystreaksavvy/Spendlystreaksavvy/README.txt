# 💰 Spendly – Personal Budget Tracker App


> Track your spending. Set your goals. Level up your financial health.

---

## 📱 App Purpose

Spendly is a mobile budget tracking app built in Kotlin for Android. It helps users manage their expenses, stay within budget goals, and build healthy financial habits using gamification. With a clean interface, Firebase integration, and visual progress dashboards, Spendly makes budgeting easy, fun, and available across devices.

---

## 🎯 Core Features


✅ *User Authentication*  
- Register and log in using email and password  
- Firebase Authentication backend

✅ *Expense Tracking*  
- Add expenses with amount, description, date, category, and optional receipt image  
- View a list of expenses filtered by a custom time range  
- Categorized expense breakdown with totals

✅ *Budget Management*  
- Set monthly spending goals (min/max)  
- Set category-specific goals  
- Visual dashboard showing progress against goals

✅ *Graphical Insights*  
- MPAndroidChart-powered bar graph  
- Displays actual spending vs category budget goals over time

✅ *Data Storage*  
- Offline persistence with RoomDB  
- Online sync with Firebase Firestore for cross-device access

✅ *Gamification*  
- Earn badges for budgeting streaks, consistent logging, and saving goals  
- Track achievements via profile

✅ *Photo Attachments*  
- Snap or upload receipts for each expense

✅ *Modern UI*  
- Material 3 design with dark/light mode toggle  
- Bottom navigation bar for intuitive movement between key screens

---

## 🧠 Design Considerations

Spendly was designed with the following in mind:

- *Simplicity:* A minimal UI with intuitive navigation and large tap targets.
- *Gamification:* Badges and goal progress encourage user return and motivation.
- *Accessibility:* Dark mode support and accessible fonts/colors.
- *Scalability:* Firebase allows cross-device usage and future analytics expansion.
- *Data Safety:* Input validation, null-checks, and error handling prevent crashes.

### 🖼 Sample Screenshots

![2025-06-09_22h38_26](https://github.com/user-attachments/assets/5f388de6-25d1-4126-9c85-d7d9cc3b4f7c)
![2025-06-09_22h38_19](https://github.com/user-attachments/assets/01bc058b-b826-40d8-b7e3-fea9158191e2)
![2025-06-09_22h38_13](https://github.com/user-attachments/assets/61de61bd-5f0c-47c3-b0ea-e3e0df1ba2aa)
![2025-06-09_22h38_08](https://github.com/user-attachments/assets/5910f4dd-00ae-43b8-b4db-e0982f89c608)
![2025-06-09_22h38_02](https://github.com/user-attachments/assets/c97c8d55-f6f9-48ba-a796-f116b89333da)
![2025-06-09_22h37_38](https://github.com/user-attachments/assets/bd320df3-7a24-4a48-8587-b36e4bb9743c)


---

## ✨ Our Own Features (Student Enhancements)

1. *Streak System & Badges* 🏆  
   Encourages consistent tracking by rewarding daily/weekly/monthly usage streaks.

2. *Budget Flex Alerts* 🔔  
   Dynamically notifies users when nearing or exceeding a category budget via animated color changes and badge unlocks.

---

## 🧪 Testing & GitHub Actions

Automated testing and builds are integrated using GitHub Actions.

*CI Features Include:*
- Automatic build on push to main branch
- Lint checks
- Unit tests (Expense and Budget logic)

> GitHub Actions Config:  
> [View .yml](.github/workflows/build.yml)

---

## 📦 APK Download

➡ https://github.com/VCWVL/prog7313-poe-Ntobekoprody/edit/main/Spendlystreaksavvy/Spendlystreaksavvy/README.txt

---

## 🎥 Demo Video

📺 

> Recorded directly from a mobile phone showing all app features with narration.

---

## ☁ Firebase Integration

- *Authentication*: Email-based login system  
- *Firestore*: Stores expenses, budgets, and user metadata  
- *Storage*: Optional receipt images uploaded to Firebase Storage

---

## 🧠 Design Considerations

- Minimalist design with focus on usability
- Gamified UI to boost engagement
- Graphs for insight-driven financial decisions
- Dark/light theme for accessibility

---

## 🧑‍💻 Tech Stack

- *Language:* Kotlin
- *UI:* Jetpack Compose + Material3
- *DB:* Room (local), Firebase Firestore (cloud)
- *Auth:* Firebase Auth
- *Charts:* MPAndroidChart
- *CI/CD:* GitHub Actions

---

## 📚 References

- Firebase Docs – https://firebase.google.com/docs
- MPAndroidChart – https://github.com/PhilJay/MPAndroidChart
- GitHub Actions – https://docs.github.com/en/actions
- Material Design – https://m3.material.io

---

© 2025 The Independent Institute of Education | PROG7313
