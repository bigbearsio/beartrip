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

    template.likeSend = (function(e) {
      firebase.database().ref('deciding/' + e.model.get('item.id')).set({
        id: e.model.get('item.id'),
        name: e.model.get('item.name'),
        photo: e.model.get('item.photo'),
        score: e.model.get('item.score') + 1
      });
      e.model.set('item.score',e.model.get('item.score'));
    }).bind(this);

    template.saveToDecided = (function(e) {
      this.saveToDecided(e);
      firebase.database().ref('deciding/').remove();

     var l = this.template.deciding.length;

      for(var i=0;i<l;i++) {
        this.template.pop("deciding");
      }

      console.log(this.template);
      
    }).bind(this);

    template.messageList = [];
    template.decided = [];
    template.deciding = [];
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
    this.messagesRef = this.database.ref('messages');
    this.messagesRef.off();

    this.decidedRef = this.database.ref('decided');
    this.decidedRef.off();

    this.decidingRef = this.database.ref('deciding');
    this.decidingRef.off();

    var setDecided = function(data) {
      var val = data.val();

      var temp = Array.prototype.splice.call(this.template.decided, 0);
      temp.push(val);
      this.template.decided = temp;

    }.bind(this);

    this.decidedRef.limitToLast(12).on('child_added', setDecided);
    //this.decidedRef.limitToLast(12).on('child_changed', setDecided);

    var addLikeDeciding = function(id) {
      firebase.database().ref('deciding/' + id).set({
        username: name,
        email: email,
        profile_picture : imageUrl
      });
    }.bind(this);

    var setDeciding = function(data) {
      var val = data.val();

      var temp = Array.prototype.splice.call(this.template.deciding, 0);
      temp.push(val);
      this.template.deciding = temp;

    }.bind(this);

    var decidingChanged = function(data) {
      this.updateDeciding(data.val());
    }.bind(this);
    

    this.decidingRef.limitToLast(12).on('child_added', setDeciding);
    this.decidingRef.limitToLast(12).on('child_changed', decidingChanged);

    var setMessage = function(data) {
      var val = data.val();
      this.displayMessage(data.key, val.name, val.text, val.photoUrl, val.created);
    }.bind(this);
    this.messagesRef.limitToLast(12).on('child_added', setMessage);
    this.messagesRef.limitToLast(12).on('child_changed', setMessage);
  };

  // Saves deciding to decided.
  HuskeyChat.prototype.saveToDecided = function(e) {
    e.preventDefault();
    var item = e.model.get('item');

      // Add a new message entry to the Firebase Database.
      this.decidedRef.push(item).then(function() {
        //this.template.input = '';
      }.bind(this)).catch(function(error) {
        console.error('Error writing new message to Firebase Database', error);
      });
  };

  // Saves a new message on the Firebase DB.
  HuskeyChat.prototype.saveMessage = function(e) {
    e.preventDefault();
    // Check that the user entered a message and is signed in.
    if (this.template.input && this.checkSignedInWithMessage()) {
      var currentUser = this.auth.currentUser;
      var profileUrl = currentUser.photoURL ||  '/images/profile_placeholder.png';
      var createdDate = new Date().toISOString();

      // Add a new message entry to the Firebase Database.
      this.messagesRef.push({
        name: currentUser.displayName,
        text: this.template.input,
        photoUrl: profileUrl,
        created: createdDate
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
    this.auth.signInWithRedirect(provider);
  };

  // Signs-out of Friendly Chat.
  HuskeyChat.prototype.signOut = function() {
    // Sign out of Firebase.
    this.auth.signOut();
  };

  // Triggers when the auth state change for instance when the user signs-in or signs-out.
  HuskeyChat.prototype.onAuthStateChanged = function(user) {
    var user;
    this.auth.getRedirectResult().then(function(result) {
      if (result.credential) {
        // This gives you a GitHub Access Token. You can use it to access the GitHub API.
        var token = result.credential.accessToken;
        // ...
      }
      // The signed-in user info.
      user = result.user;
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
    if (user) { // User is signed in!
      // Get profile pic and user's name from the Firebase user object.
      var profilePicUrl = (user.photoURL || '/images/profile_placeholder.png');
      var userName = user.displayName;
      var avatarColor = fromUserToColor(userName);

      this.template.uuid = userName;
      this.template.avatar = profilePicUrl;
      this.template.color = avatarColor;

      // We load currently existing chant messages.
      this.loadMessages();
    } else { // User is signed out!
      this.signIn()
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
  HuskeyChat.prototype.displayMessage = function(key, name, text, picUrl, created) {
    var temp = Array.prototype.splice.call(this.template.messageList, 0);
    temp.push({
      color: fromUserToColor(name),
      avatar: picUrl,
      text: text,
      uuid: name,
      timestamp: created
    });
    this.template.messageList = temp;

    this.template.async(function () {
      var chatDiv = document.querySelector('.chat-list');
      chatDiv.scrollTop = chatDiv.scrollHeight; //TODO: Need to fix so that we can find the .chat-list class object
    });

  };

  HuskeyChat.prototype.updateDeciding = function(data) {
    //var temp = Array.prototype.splice.call(this.template.deciding, 0);
    var temp = this.template.deciding.slice(0);
    
    temp.forEach(function(o){
      if (o.id == data.id){

        o.score = data.score;
      }
    });
    
    var l = this.template.deciding.length;

    for(var i=0;i<l;i++) {
      this.template.deciding.pop();
    }

    for(var i=0;i<l;i++) {
      this.template.deciding.push(temp[i]);
    }


    console.log(this.template.deciding);
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

  // avatar colors
  var colors = ['navy', 'slate', 'olive', 'moss', 'chocolate', 'buttercup', 'maroon', 'cerise', 'plum', 'orchid'];

  function fromUserToColor(userName) {
    var colorIndex = Array.prototype.reduce.call(userName, function (acc, x) { return acc + x.charCodeAt(0); }, 0) % colors.length;

    return colors[colorIndex];
  }


  window.HuskeyChat = HuskeyChat;

})();

(function() {
  'use strict';


  window.onload = function() {
      /* Polymer UI and UX */

      var template = document.querySelector('template[is=dom-bind]');

      setTimeout(function () {

          if (window.innerWidth < 800) {
              var drawer = document.querySelector('#drawerPanel');
              drawer.drawerWidth = '95%';
          }
      }, 0);

      var chat = new HuskeyChat(template);

      

  };
  console.log("check install worker");
  if ('serviceWorker' in navigator) {
        navigator.serviceWorker
                .register('./service-worker.js')
                .then(function() { console.log('Service Worker Registered'); });
  }


})();

