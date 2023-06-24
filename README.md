# Project_SecureData
Created an android application that uses the components of android (Activity, BroadCast Recievers, Services, Content Provider)
This application gets the user's current location, connectivity status, battery status after a fixed amount of time which is set by the user and stores them in the firebase
firestore and if the data is not stored in the firestore due to connectivity issues, the data gets stored in the local database and upon connecting to the internet, the data is
again stored in the firebase firestore.
This application also has the option to capture a photo using camera or choose a photo from gallery and store the photo in the firebase cloud storage as well.
