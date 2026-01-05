<?php
/**
 * get_approved_donations.php
 * Fetches all approved donation requests that are not yet paid
 */

header('Content-Type: application/json');
header('Access-Control-Allow-Origin: *');
header('Access-Control-Allow-Methods: GET, POST');
header('Access-Control-Allow-Headers: Content-Type');

// Database connection
$host = 'localhost';
$dbname = 'savepaws';
$username = 'root';
$password = '';

try {
    $conn = new PDO("mysql:host=$host;dbname=$dbname;charset=utf8mb4", $username, $password);
    $conn->setAttribute(PDO::ATTR_ERRMODE, PDO::ERRMODE_EXCEPTION);
} catch(PDOException $e) {
    echo json_encode([
        'success' => false,
        'message' => 'Database connection failed: ' . $e->getMessage()
    ]);
    exit;
}

try {
    // Query to fetch approved donations that are not paid
    $query = "
        SELECT 
            d.donation_id,
            d.center_id,
            d.case_id,
            d.image_of_animal,
            d.amount,
            d.requested_time,
            d.approval_status,
            d.donation_status,
            c.center_name,
            c.phone AS center_phone,
            cs.type_of_animal,
            cs.animal_condition,
            cs.photo AS case_photo
        FROM donations d
        LEFT JOIN centers c ON d.center_id = c.center_id
        LEFT JOIN cases cs ON d.case_id = cs.case_id
        WHERE d.approval_status = 'Approved' 
        AND (d.donation_status IS NULL OR d.donation_status != 'Paid')
        ORDER BY d.requested_time DESC
    ";
    
    $stmt = $conn->prepare($query);
    $stmt->execute();
    $donations = $stmt->fetchAll(PDO::FETCH_ASSOC);
    
    if (count($donations) > 0) {
        $formattedDonations = [];
        foreach ($donations as $donation) {
            $formattedDonations[] = [
                'donation_id' => intval($donation['donation_id']),
                'center_id' => intval($donation['center_id']),
                'case_id' => intval($donation['case_id']),
                'image_of_animal' => $donation['image_of_animal'],
                'amount' => floatval($donation['amount']),
                'requested_time' => $donation['requested_time'],
                'center_name' => $donation['center_name'],
                'center_phone' => $donation['center_phone'],
                'animal_type' => $donation['type_of_animal'],
                'animal_condition' => $donation['animal_condition'],
                'case_photo' => $donation['case_photo']
            ];
        }
        
        echo json_encode([
            'success' => true,
            'message' => 'Donations fetched successfully',
            'total_donations' => count($formattedDonations),
            'donations' => $formattedDonations
        ]);
    } else {
        echo json_encode([
            'success' => true,
            'message' => 'No approved donations found',
            'total_donations' => 0,
            'donations' => []
        ]);
    }
    
} catch(PDOException $e) {
    echo json_encode([
        'success' => false,
        'message' => 'Query failed: ' . $e->getMessage()
    ]);
}

$conn = null;
?>
