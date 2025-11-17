// Firebase configuration
const firebaseConfig = {
    apiKey: "AIzaSyApPAAsFe_GbZvH8cCpSr5wvfuOO0TFK3I",
    authDomain: "cofttech-20762.firebaseapp.com",
    projectId: "cofttech-20762",
    storageBucket: "cofttech-20762.firebasestorage.app",
    messagingSenderId: "602495114731",
    appId: "1:602495114731:web:982053505e3233ee0e0dae",
    measurementId: "G-E1HS8WT6T6"
  };

firebase.initializeApp(firebaseConfig);

// References to auth elements
const authStatus = document.getElementById('auth-status');
const authButtons = document.getElementById('auth-buttons');
const userProfile = document.getElementById('user-profile');
const userEmail = document.getElementById('user-email');
const logoutBtn = document.getElementById('logout-btn');
const loginForm = document.getElementById('login-form');
const signupForm = document.getElementById('signup-form');
const loginError = document.getElementById('login-error');
const signupError = document.getElementById('signup-error');

// Authentication state observer
firebase.auth().onAuthStateChanged((user) => {
    if (user) {
        // User is signed in
        console.log('User is signed in:', user.email);
        authButtons.classList.add('d-none');
        userProfile.classList.remove('d-none');
        userEmail.textContent = user.email;
        
        // Show welcome message
        authStatus.classList.remove('d-none', 'alert-danger');
        authStatus.classList.add('alert-success');
        authStatus.textContent = `Welcome, ${user.email}! You are now logged in.`;
        
        // Close any open modals
        const modals = document.querySelectorAll('.modal');
        modals.forEach(modal => {
            const modalInstance = bootstrap.Modal.getInstance(modal);
            if (modalInstance) {
                modalInstance.hide();
            }
        });
        
        // Send user data to backend API (backend has admin permissions)
        sendUserToBackend(user);
    } else {
        // User is signed out
        console.log('User is signed out');
        authButtons.classList.remove('d-none');
        userProfile.classList.add('d-none');
        userEmail.textContent = '';
        
        // Hide status message after logout
        authStatus.classList.add('d-none');
    }
});

// Send user data to our backend API (backend uses Admin SDK with full permissions)
function sendUserToBackend(user) {
    fetch('/api/users', {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json',
        },
        body: JSON.stringify({
            email: user.email,
            uid: user.uid
        }),
    })
    .then(response => response.json())
    .then(data => {
        console.log('Success sending user data to backend:', data);
    })
    .catch((error) => {
        console.error('Error sending user data to backend:', error);
    });
}

// Login form submission
if (loginForm) {
    loginForm.addEventListener('submit', (e) => {
        e.preventDefault();
        
        const email = document.getElementById('login-email').value;
        const password = document.getElementById('login-password').value;
        
        // Hide previous error messages
        loginError.classList.add('d-none');
        
        // Sign in with email and password
        firebase.auth().signInWithEmailAndPassword(email, password)
            .catch((error) => {
                console.error('Login error:', error);
                loginError.textContent = error.message;
                loginError.classList.remove('d-none');
            });
    });
}

// Signup form submission
if (signupForm) {
    signupForm.addEventListener('submit', (e) => {
        e.preventDefault();
        
        const email = document.getElementById('signup-email').value;
        const password = document.getElementById('signup-password').value;
        
        // Hide previous error messages
        signupError.classList.add('d-none');
        
        // Create user with email and password
        firebase.auth().createUserWithEmailAndPassword(email, password)
            .catch((error) => {
                console.error('Signup error:', error);
                signupError.textContent = error.message;
                signupError.classList.remove('d-none');
            });
    });
}

// Logout functionality
if (logoutBtn) {
    logoutBtn.addEventListener('click', (e) => {
        e.preventDefault();
        firebase.auth().signOut()
            .then(() => {
                // Show logout message
                authStatus.classList.remove('d-none', 'alert-success');
                authStatus.classList.add('alert-danger');
                authStatus.textContent = 'You have been logged out.';
                
                // Hide logout message after 3 seconds
                setTimeout(() => {
                    authStatus.classList.add('d-none');
                }, 3000);
            })
            .catch((error) => {
                console.error('Logout error:', error);
            });
    });
}

// Demo functionality for the Explore button
const exploreBtn = document.getElementById('explore-btn');
if (exploreBtn) {
    exploreBtn.addEventListener('click', () => {
        const isLoggedIn = firebase.auth().currentUser !== null;
        
        if (isLoggedIn) {
            alert('Thank you for exploring Toronto! Check out our latest features.');
        } else {
            // Show login prompt
            const loginModal = new bootstrap.Modal(document.getElementById('loginModal'));
            loginModal.show();
        }
    });
}

// Contact form submission
const contactForm = document.querySelector('#contact form');
if (contactForm) {
    contactForm.addEventListener('submit', (e) => {
        e.preventDefault();
        
        // Show popup message
        alert('Message is sent!');
        
        // Optionally, reset the form
        contactForm.reset();
    });
}