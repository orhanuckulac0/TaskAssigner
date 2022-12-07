
# Project Title

A project management app for multiple users.

## Features

This app uses Firebase platform.
Currently I am collecting user and project data on Cloud Firestore. 

Current functions of the app:
- Sign in, Sign up, Sign out functionality.
- Profile section, only name and phone number can be updated by the user.
- User can create a board and add members to it.
- Board contains Lists of Tasks.
- Tasks contains list of Cards for specific Members of the board.
- When a user is added to a board, send notification to the user. (FCM push notification)
- Boards and cards can have label colors for importance of the job.
- Due dates, board descriptions, card descriptions are also can be filled.
- Swipe to refresh on main screen and tasks screen.
- Tasks screen has drag and drop functionality for cards.
- Data collection on Cloud Firestore

User permissions:
- Board creator can delete, update, add members to board.
- Board creator has all the permissions on Tasks and Cards.
- Board creator can change card positions with drag and drop functionality.
- Members can not add members to a board, task or card.
- Members can not delete or update board, task or card or their data.
- Members can only see the boards and their extends that they are assigned to.
## Screenshots
Some sample screenshots of the app 

![splash](https://user-images.githubusercontent.com/101017069/203461881-65936f88-31b3-4812-94c0-54497e24a627.PNG)
![intro](https://user-images.githubusercontent.com/101017069/203461883-982c3d21-a0c5-447c-b55e-092926767423.PNG)
![drawer](https://user-images.githubusercontent.com/101017069/203461888-0b18b5da-7588-443d-918d-b19b8e561e8f.PNG)
![update profile](https://user-images.githubusercontent.com/101017069/203461889-907b4881-c0d9-42e7-9a49-61eb056807e0.PNG)
![main page](https://user-images.githubusercontent.com/101017069/206170216-67829ab8-70a4-4219-a86a-fe8e7964bb97.PNG)
![board details](https://user-images.githubusercontent.com/101017069/206169858-ab47b6c1-25b2-4b71-846a-99690b4226ed.PNG)
![members](https://user-images.githubusercontent.com/101017069/206169851-0391f11e-b20f-4b8d-88e9-0caaeef55ae3.PNG)
![tasklist](https://user-images.githubusercontent.com/101017069/206169861-a18be09b-5049-4b0c-8119-6761cc3da931.PNG)
![card details](https://user-images.githubusercontent.com/101017069/206169864-cf7417d7-9d0c-4d5f-af7d-1efa8ec49697.PNG)
