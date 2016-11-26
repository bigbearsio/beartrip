// firebase chat
(function() {
/**
 * Copyright 2015 Google Inc. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
'use strict';

  // Initializes HuskeyChat.
  function HuskeyChat(template) {
    this.checkSetup();
    this.template = template;

    this.initFirebase();

    // Sending a chat message by pressing a keyboard return key
    this.template.checkKey = (function(e) {
      if(e.keyCode === 13 || e.charCode === 13) {
        this.saveMessage(e);
      }
    }).bind(this);
    // Sending a chat message by clicking a "Send" button
    template.sendMyMessage = (function(e) {
      this.saveMessage(e);
    }).bind(this);

    template.messageList = [];
    template.input = '';
  }

  // Sets up shortcuts to Firebase features and initiate firebase auth.
  HuskeyChat.prototype.initFirebase = function() {
    // Shortcuts to Firebase SDK features.
    this.auth = firebase.auth();
    this.database = firebase.database();
    this.storage = firebase.storage();
    // Initiates Firebase auth and listen to auth state changes.
    this.auth.onAuthStateChanged(this.onAuthStateChanged.bind(this));
  };

  // Loads chat messages history and listens for upcoming ones.
  HuskeyChat.prototype.loadMessages = function() {
    // Reference to the /messages/ database path.
    this.messagesRef = this.database.ref('messages');
    // Make sure we remove all previous listeners.
    this.messagesRef.off();

    // Loads the last 12 messages and listen for new ones.
    var setMessage = function(data) {
      var val = data.val();
      this.displayMessage(data.key, val.name, val.text, val.photoUrl, val.imageUrl);
    }.bind(this);
    this.messagesRef.limitToLast(12).on('child_added', setMessage);
    this.messagesRef.limitToLast(12).on('child_changed', setMessage);
  };

  // Saves a new message on the Firebase DB.
  HuskeyChat.prototype.saveMessage = function(e) {
    e.preventDefault();
    // Check that the user entered a message and is signed in.
    if (this.template.input && this.checkSignedInWithMessage()) {
      var currentUser = this.auth.currentUser;
      // Add a new message entry to the Firebase Database.
      this.messagesRef.push({
        name: currentUser.displayName,
        text: this.template.input,
        photoUrl: currentUser.photoURL ||  '/images/profile_placeholder.png'
      }).then(function() {
        // Clear message text field
        this.template.input = '';
      }.bind(this)).catch(function(error) {
        console.error('Error writing new message to Firebase Database', error);
      });
    }
  };

  // Sets the URL of the given img element with the URL of the image stored in Firebase Storage.
  HuskeyChat.prototype.setImageUrl = function(imageUri, imgElement) {
    // If the image is a Firebase Storage URI we fetch the URL.
    if (imageUri.startsWith('gs://')) {
      imgElement.src = HuskeyChat.LOADING_IMAGE_URL; // Display a loading image first.
      this.storage.refFromURL(imageUri).getMetadata().then(function(metadata) {
        imgElement.src = metadata.downloadURLs[0];
      });
    } else {
      imgElement.src = imageUri;
    }
  };

  // Saves a new message containing an image URI in Firebase.
  // This first saves the image in Firebase storage.
  HuskeyChat.prototype.saveImageMessage = function(event) {
    var file = event.target.files[0];

    // Clear the selection in the file picker input.
    this.imageForm.reset();

    // Check if the file is an image.
    if (!file.type.match('image.*')) {
      var data = {
        message: 'You can only share images',
        timeout: 2000
      };
      return;
    }

    // Check if the user is signed-in
    if (this.checkSignedInWithMessage()) {

      // We add a message with a loading icon that will get updated with the shared image.
      var currentUser = this.auth.currentUser;
      this.messagesRef.push({
        name: currentUser.displayName,
        imageUrl: HuskeyChat.LOADING_IMAGE_URL,
        photoUrl: currentUser.photoURL || '/images/profile_placeholder.png'
      }).then(function(data) {
        // Upload the image to Firebase Storage.
        this.storage.ref(currentUser.uid + '/' + Date.now() + '/' + file.name)
          .put(file, {contentType: file.type})
          .then(function(snapshot) {
            // Get the file's Storage URI and update the chat message placeholder.
            var filePath = snapshot.metadata.fullPath;
            data.update({imageUrl: this.storage.ref(filePath).toString()});
          }.bind(this)).catch(function(error) {
            console.error('There was an error uploading a file to Firebase Storage:', error);
          });
      }.bind(this));
    }
  };

  // Signs-in Friendly Chat.
  HuskeyChat.prototype.signIn = function() {
    // Sign in Firebase using popup auth and Google as the identity provider.
    var provider = new firebase.auth.GoogleAuthProvider();
    //this.auth.signInWithPopup(provider);
    //this.auth.signInWithRedirect(provider);
    console.log('try login');
    firebase.auth().signInWithRedirect(provider);
  };

  // Signs-out of Friendly Chat.
  HuskeyChat.prototype.signOut = function() {
    // Sign out of Firebase.
    this.auth.signOut();
  };

  // Triggers when the auth state change for instance when the user signs-in or signs-out.
  HuskeyChat.prototype.onAuthStateChanged = function(user) {
    console.log("checking user status");
    var user;
    this.auth.getRedirectResult().then(function(result) {
      if (result.credential) {
        var token = result.credential.accessToken;
        // ...
      }
      
    }).catch(function(error) {
      // Handle Errors here.
      var errorCode = error.code;
      var errorMessage = error.message;
      // The email of the user's account used.
      var email = error.email;
      // The firebase.auth.AuthCredential type that was used.
      var credential = error.credential;
      // ...
    });

      // The signed-in user info.
      //var user = result.user;
      if (user) { // User is signed in!
        console.log("login");
        // Get profile pic and user's name from the Firebase user object.
        var profilePicUrl = user.photoURL;
        var userName = user.displayName;
        //template.channel = 'polymer-chat';
        //template.cats = [];
        this.template.uuid = userName;
        this.template.avatar = (profilePicUrl || '/images/profile_placeholder.png');
        this.template.color = 'green';

        // We load currently existing chant messages.
        this.loadMessages();
      } else { // User is signed out!
        //console.log("not login");
        this.signIn()

        //this.loadMessages();
      }
    
  };

  // Returns true if user is signed-in. Otherwise false and displays a message.
  HuskeyChat.prototype.checkSignedInWithMessage = function() {
    // Return true if the user is signed in Firebase
    if (this.auth.currentUser) {
      return true;
    }

    // Display a message to the user using a Toast.
    var data = {
      message: 'You must sign-in first',
      timeout: 2000
    };
    return false;
  };

  // Template for messages.
  HuskeyChat.MESSAGE_TEMPLATE =
    '<div class="message-container">' +
    '<div class="spacing"><div class="pic"></div></div>' +
    '<div class="message"></div>' +
    '<div class="name"></div>' +
    '</div>';

  // A loading image URL.
  HuskeyChat.LOADING_IMAGE_URL = 'https://www.google.com/images/spin-32.gif';

  // Displays a Message in the UI.
  HuskeyChat.prototype.displayMessage = function(key, name, text, picUrl, imageUri) {
    var temp = Array.prototype.splice.call(this.template.messageList, 0);
    temp.push({
      color: 'blue',
      avatar: imageUri,
      text: text,
      uuid: name,
      timestamp: new Date()
    });
    this.template.messageList = temp;

    this.template.async(function () {
      var chatDiv = document.querySelector('.chat-list');
      chatDiv.scrollTop = chatDiv.scrollHeight; //TODO: Need to fix so that we can find the .chat-list class object
    });

  };

  // Enables or disables the submit button depending on the values of the input
  // fields.
  HuskeyChat.prototype.toggleButton = function() {
    if (this.messageInput.value) {
      this.submitButton.removeAttribute('disabled');
    } else {
      this.submitButton.setAttribute('disabled', 'true');
    }
  };

  // Checks that the Firebase SDK has been correctly setup and configured.
  HuskeyChat.prototype.checkSetup = function() {
    if (!window.firebase || !(firebase.app instanceof Function) || !window.config) {
      window.alert('You have not configured and imported the Firebase SDK. ' +
        'Make sure you go through the codelab setup instructions.');
    } else if (config.storageBucket === '') {
      window.alert('Your Firebase Storage bucket has not been enabled. Sorry about that. This is ' +
        'actually a Firebase bug that occurs rarely. ' +
          'Please go and re-generate the Firebase initialisation snippet (step 4 of the codelab) ' +
          'and make sure the storageBucket attribute is not empty. ' +
          'You may also need to visit the Storage tab and paste the name of your bucket which is ' +
          'displayed there.');
    }
  };

  window.HuskeyChat = HuskeyChat;

})();

(function() {
  'use strict';


  window.onload = function() {
      /* Polymer UI and UX */

      var template = document.querySelector('template[is=dom-bind]');

      var chat = new HuskeyChat(template);

  };


})();

