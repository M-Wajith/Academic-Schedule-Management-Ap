const functions = require('firebase-functions');
const admin = require('firebase-admin');
admin.initializeApp();

exports.deleteUser = functions.https.onCall((data, context) => {
    const email = data.email;
    console.log(`Attempting to delete user with email: ${email}`);
    
    return admin.auth().getUserByEmail(email)
        .then((userRecord) => {
            const uid = userRecord.uid;
            return admin.auth().deleteUser(uid)
                .then(() => {
                    return admin.firestore().collection('users').doc(uid).delete();
                });
        })
        .then(() => {
            return { message: 'User deleted successfully' };
        })
        .catch((error) => {
            console.error("Error deleting user:", error);
            throw new functions.https.HttpsError('unknown', error.message, error);
        });
});
