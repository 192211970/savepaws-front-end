<?php
header("Content-Type: application/json");
include("db.php");

// Get total cases and breakdown by status
$casesQuery = $conn->query("
    SELECT 
        COUNT(*) as total_cases,
        SUM(CASE WHEN status = 'Pending' THEN 1 ELSE 0 END) as pending_cases,
        SUM(CASE WHEN status = 'In_Progress' THEN 1 ELSE 0 END) as in_progress_cases,
        SUM(CASE WHEN status = 'Closed' THEN 1 ELSE 0 END) as closed_cases
    FROM cases
");
$casesStats = $casesQuery->fetch_assoc();

// Get center counts
$centersQuery = $conn->query("
    SELECT 
        COUNT(*) as total_centers,
        SUM(CASE WHEN is_active = 1 THEN 1 ELSE 0 END) as active_centers
    FROM centers
");
$centersStats = $centersQuery->fetch_assoc();

// Get donation counts
$donationsQuery = $conn->query("
    SELECT 
        COUNT(*) as total_donations,
        SUM(CASE WHEN approval_status = 'Pending' THEN 1 ELSE 0 END) as pending_donations,
        SUM(CASE WHEN approval_status = 'Approved' THEN 1 ELSE 0 END) as approved_donations,
        SUM(CASE WHEN approval_status = 'Rejected' THEN 1 ELSE 0 END) as rejected_donations
    FROM donations
");
$donationsStats = $donationsQuery->fetch_assoc();

echo json_encode([
    "success" => true,
    "stats" => [
        "total_cases" => (int)($casesStats['total_cases'] ?? 0),
        "pending_cases" => (int)($casesStats['pending_cases'] ?? 0),
        "in_progress_cases" => (int)($casesStats['in_progress_cases'] ?? 0),
        "closed_cases" => (int)($casesStats['closed_cases'] ?? 0),
        "total_centers" => (int)($centersStats['total_centers'] ?? 0),
        "active_centers" => (int)($centersStats['active_centers'] ?? 0),
        "total_donations" => (int)($donationsStats['total_donations'] ?? 0),
        "pending_donations" => (int)($donationsStats['pending_donations'] ?? 0),
        "approved_donations" => (int)($donationsStats['approved_donations'] ?? 0),
        "rejected_donations" => (int)($donationsStats['rejected_donations'] ?? 0)
    ]
]);
?>
