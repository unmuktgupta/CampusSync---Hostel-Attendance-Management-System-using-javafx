# CampusSync - Hostel Attendance Management System 🏢

CampusSync is a modern, premium desktop application built with **JavaFX** to handle daily student hostel attendance. The system replaces traditional paper registers with a digital dashboard that allows wardens to seamlessly onboard students, track their location, and assign real-time statuses (Present, Absent, or On Leave).

![CampusSync Dashboard](https://img.shields.io/badge/UI-JavaFX-blue?style=for-the-badge&logo=java)

## 🌟 Features
* **Student Onboarding:** Register students based on their details (Name, Roll No., Room No., and Block).
* **Live Dashboard:** Keep track of how many students are currently marked **Present**, **Absent**, or **On Leave** via dynamic statistic chips. 
* **State Management:** Fully functional data persistence across sessions—the queue will automatically reload all information upon launching the app. 
* **Background Processing:** Features simulated asynchronous verification tasks:
    * **Biometric verification** simulation when marking someone Present.
    * **SMS Gateway alert** simulation when marking a student Absent.
    * **Ledger update processing** when confirming Leave documents.
* **Premium Dark Mode GUI:** Designed with rich, high-contrast dark accents, interactive linear gradients, and state-based row highlighting in the attendance logs.

## 🛠️ Technology Stack
* **Language:** Java 24
* **Framework:** JavaFX 24 SDK
* **Styling:** Vanilla CSS (JavaFX styling protocols)

## 🚀 How to Run

### Option 1: One-Click Run (Windows)
We've included a simple batch script to launch the application immediately without IDE configurations.
1. Simply double-click **`run.bat`**. 
2. Alternatively, open PowerShell/Command Prompt in this directory and type:
   ```powershell
   .\run.bat
   ```

### Option 2: Run via VS Code
1. Open this repository folder in VS Code.
2. Ensure you have the **Extension Pack for Java** installed.
3. Open `src/hostel/HostelApp.java`.
4. Navigate to the `Run and Debug` tab (or hit `F5`) and launch the pre-configured **HostelApp** profile!

### Prerequisites & Dependencies
* Ensure that the **JavaFX SDK** is extracted to `C:\javafx-sdk-24`. The workspace configurations (`.vscode/launch.json`, `.vscode/settings.json`, and `run.bat`) are hardcoded to utilize the JavaFX library exactly at `C:\javafx-sdk-24\lib`. 
* If your JavaFX is located elsewhere, be sure to update the paths in those files accordingly!

---
*Built as a dedicated attendance tracker to revolutionize hostel administrative systems.*
