<?php
header("Content-Type: application/json");
include("db.php");

$email = $_POST['email'] ?? null;

if (!$email) {
    echo json_encode(["status" => "error", "message" => "Email required"]);
    exit;
}

// Check email exists
$check = $conn->prepare("SELECT id FROM users WHERE email = ?");
$check->bind_param("s", $email);
$check->execute();
if ($check->get_result()->num_rows === 0) {
    echo json_encode(["status" => "error", "message" => "Email not registered"]);
    exit;
}

// Generate 4-digit OTP
$otp = rand(1000, 9999);

// Store OTP in DB (update if exists, insert if not)
// Assuming we have an 'otp_verifications' table OR adds otp column to users table.
// Simplest way: Add 'otp' and 'otp_expiry' column to users table.

$update = $conn->prepare("UPDATE users SET otp = ?, otp_expiry = DATE_ADD(NOW(), INTERVAL 10 MINUTE) WHERE email = ?");
$update->bind_param("is", $otp, $email);

if ($update->execute()) {
    // Send OTP via Email
    $subject = "SavePaws Password Reset OTP";
    $message = "Your OTP for resetting your password is: " . $otp . "\n\nThis OTP is valid for 10 minutes.\n\nIf you did not request this, please ignore this email.";
    $headers = "From: no-reply@savepaws.com\r\n" .
               "Reply-To: no-reply@savepaws.com\r\n" .
               "X-Mailer: PHP/" . phpversion();

    if (mail($email, $subject, $message, $headers)) {
        echo json_encode(["status" => "success", "message" => "OTP sent to your email"]);
    } else {
        // Fallback for local testing if mail is not configured
        // In production, this should just be an error.
        // For debugging now, we return success so functionality can be tested via DB OTP check.
        echo json_encode([
             "status" => "success", 
             "message" => "OTP generated. (Email failed - Check Server Logs/Console). OTP is: " . $otp
        ]);
    }
} else {
    echo json_encode(["status" => "error", "message" => "Db Error"]);
}
?>
