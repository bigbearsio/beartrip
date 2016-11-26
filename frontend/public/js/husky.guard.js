var HuskyGuard = {
    forceLogin: function() {
        firebase.auth().onAuthStateChanged(function(user) {
            if (user != null) {
                //console.log(HuskyGuard.getFirebaseUser());
            } else {
                if(!HuskyGuard.isFirebaseLogin()) {
                    HuskyGuard.doFirebaseLogin();
                }
            }
        });
    },

    doFirebaseLogin: function() {
        var provider = new firebase.auth.GoogleAuthProvider();
        provider.addScope('https://www.googleapis.com/auth/plus.login');
        provider.setCustomParameters({
            'login_hint': 'user@example.com'
        });

        // login with Google
        firebase.auth().signInWithPopup(provider).then(function(result) {
            var token = result.credential.accessToken;
            var user = result.user;
        }).catch(function(error) {
            var errorCode = error.code;
            var errorMessage = error.message;
            var email = error.email;
            var credential = error.credential;
        });
    },

    doFirebaseLogout: function() {
        firebase.auth().signOut().then(function() {
        // Sign-out successful.
        }, function(error) {
        // An error happened.
        });
    },

    isFirebaseLogin: function() {
        var user = firebase.auth().currentUser;

        if (user != null) {
            return true;
        } else {
            return false;
        }
    },

    getFirebaseUser: function() {
        var user = firebase.auth().currentUser;

        if (user != null) {
            return {
                name : user.displayName,
                email : user.email,
                photoUrl : user.photoURL,
                uid : user.uid 
            };
        }
    }
}