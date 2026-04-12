# Lily - Marketing Agent

### Introduction

Lily is a social media marketing agent that helps you manage multiple social media platforms from content creation to influencer discovery, post discovery and analyzing metrics using a chat interface. Just give your query or use one of the 5 prebuilt pipelines and the agent will handle it all for you!

---

### Screenshots

<!-- LEAVE BLANK ILL UPDATE -->

---

### Instructions

#### Development Setup

1. **Clone the Repository**
   ```bash
   git clone <repository-url>
   ```

2. **Open the Project**
   * Open **Android Studio**.
   * Select **Open** and navigate to the `MobileApp/MarketingAgent` directory.
   * Wait for Gradle to fully sync the project dependencies.

3. **Configure API Endpoints**
   * Open `ApiConfig.java` located at `app/src/main/java/com/mota/marketingagent/data/ApiConfig.java`.
   * Ensure `BASE_URL` is pointing to the correct API Gateway (e.g. your Azure or ngrok URL).

4. **Run the App**
   * Connect an Android device via USB or start a virtual emulator.
   * Click the green **Run** button in Android Studio, or execute the following Gradle command from the root directory:
     ```bash
     ./gradlew assembleDebug
     ```
