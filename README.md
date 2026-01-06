# Link-Work

**Team Members:**
- **Yohannes Desta** - UGR/35682/16 (Frontend)
- **Biniyam Teku** - UGR/34102/16 (Frontend)
- **Nuredin Wario** - UGR/35196/16 (Backend)
- **Betel Habtemariam** - UGR/34042/16 (Frontend)
- **Menwuyelet Temesgen** - UGR/34920/16 (Backend)
- **Sosina Anteneh** - UGR/35444/16 (Backend)


**Link-Work** is a modern freelance marketplace platform designed to connect institutions with skilled freelancers. Built with **Java Servlets**, **MySQL**, and **WebSockets**, it features a stunning glassmorphism UI, real-time messaging, and dedicated dashboards for streamlined job management and application tracking.

## 🚀 Features

### For Institutions (Employers)
*   **Job Posting**: Create detailed job announcements with titles, descriptions, and budgets.
*   **Applicant Management**: View list of applicants for each job.
*   **Direct Communication**: Initiate real-time chats with applicants directly from the dashboard.
*   **Secure Dashboard**: Manage all recruitment activities in one place.

### For Freelancers (Job Seekers)
*   **Job Discovery**: Browse a wide range of job opportunities in various categories (Development, Design, Marketing, etc.).
*   **Easy Application**: Apply to jobs with a single click.
*   **Application Tracking**: See all applied jobs and their statuses.
*   **Real-time Chat**: Chat with potential employers to discuss requirements and interviews.

### Core System Features
*   **Role-Based Authentication**: Secure Login and Signup for "Institute" and "Job Seeker" roles.
*   **Real-Time Chat System**: Built with WebSockets for instant messaging.
*   **Persistent Data**: MySQL database stores users, jobs, applications, and chat history.
*   **Responsive Design**: A beautiful, glassmorphic UI that works on desktops and tablets.

---

## 🛠️ Technology Stack

*   **Backend**: Java 25, Servlet API 4.0
*   **Database**: MySQL 9.0
*   **Real-time Communication**: Java WebSockets (`javax.websocket`)
*   **Frontend**: HTML5, CSS3 (Custom Glassmorphism Design), JavaScript (Vanilla)
*   **Build Tool**: Apache Maven
*   **Server**: Apache Tomcat 9

---

## 📂 Project Structure

```bash
link-work/
├── database/
│   └── database.sql             # SQL script to initialize the database
├── src/
│   └── main/
│       ├── java/com/project/
│       │   ├── db/              # Database Connection & Helper classes
│       │   │   ├── DB.java
│       │   │   └── ChatStorage.java
│       │   ├── servlet/         # REST API Endpoints (Auth, Jobs, Applications)
│       │   │   ├── LoginServlet.java
│       │   │   ├── SignupServlet.java
│       │   │   ├── LogoutServlet.java
│       │   │   ├── AnnouncementServlet.java
│       │   │   ├── ApplyServlet.java
│       │   │   ├── ApplicantsServlet.java
│       │   │   ├── AppliedJobsServlet.java
│       │   │   ├── ChatConversationsServlet.java
│       │   │   ├── ChatMessagesServlet.java
│       │   │   ├── ChatSendServlet.java
│       │   │   └── ChatUnreadServlet.java
│       │   └── websocket/       # WebSocket implementation for Chat
│       │       ├── ChatWebSocketEndpoint.java
│       │       └── GetHttpSessionConfigurator.java
│       └── webapp/              # Frontend Assets
│           ├── css/
│           │   ├── global.css    # Variables, Reset, Utilities
│           │   ├── index.css     # Homepage styles
│           │   ├── auth.css      # Login/Signup/Forms
│           │   ├── dashboard.css # Dashboard specific styles
│           │   └── chat.css      # Chat interface styles
│           ├── js/
│           │   ├── chat.js       # Chat client logic
│           │   └── toast.js      # Toast notification logic
│           ├── index.html        # Landing Page
│           ├── login.html        # Authentication Pages
│           ├── signup.html
│           ├── institutionDashboard.html
│           ├── freelancerDashboard.html
│           └── chat.html        # Chat Interface
│  
└── pom.xml                      # Maven Dependencies
```

---

## ⚙️ Setup & Installation

### 1. Prerequisites
*   Java JDK 
*   Apache Maven
*   MySQL Server
*   Apache Tomcat 9

### 2. Database Setup
1.  Open your MySQL Client (Workbench or Command Line).
2.  Run the script located at `database/database.sql` to create the `worklink_db` and tables.
3.  **Important**: Update database credentials in `src/main/java/com/project/db/DB.java`:
    ```java
    private static final String URL = "jdbc:mysql://localhost:3306/link-work_db";
    private static final String USER = "root"; // Your MySQL Username
    private static final String PASSWORD = "your_password"; // Your MySQL Password
    ```

### 3. Build the Project
Open a terminal in the project root directory and run:
```bash
mvn clean package
```
This will generate a `.war` file in the `target/` directory.

### 4. Deploy
*   Copy the generated `link-work.war` file to your `Tomcat webapps` folder.
*   Start the Tomcat server.
*   Access the application at: `http://localhost:8080/link-work/`

### 5. Quick Run Commands (Windows)

To quickly rebuild the project and restart the Tomcat server, simply copy and paste the following commands into your terminal.

**Note:** Replace the path in the first command with the actual location of your project folder.

```powershell
# 1. Navigate to project root
cd "C:\Users\Downloads\link-work"

# 2. Build the project
mvn clean package

# 3. Startup Tomcat
"C:\tomcat\bin\startup.bat"

# 4. Shutdown Tomcat (if running)
"C:\tomcat\bin\shutdown.bat"

```

Access the application at: `http://localhost:8080/link-work/`

## 👥 Members List
**Group Name:**

1. Student 1 - ID: [ID]
2. Student 2 - ID: [ID]
3. Student 3 - ID: [ID]
4. Student 4 - ID: [ID]
5. Student 5 - ID: [ID]
6. Student 6 - ID: [ID]
