<?php
header("Content-Type: application/json");
include("db.php");

/*
|--------------------------------------------------------------------------
| ADMIN – PENDING CASES (Not yet accepted)
|--------------------------------------------------------------------------
| Cases where status is Pending (not accepted by any center)
| Classified as Critical or Standard based on remark in case_escalations
|--------------------------------------------------------------------------
*/

$sql = "
SELECT
    c.case_id,
    c.user_id,
    c.type_of_animal,
    c.animal_condition,
    c.photo,
    c.latitude,
    c.longitude,
    c.created_time,
    c.status AS case_status,

    TIMESTAMPDIFF(MINUTE, c.created_time, NOW()) AS case_age_minutes,

    /* All centers notified (ignore Resent rows) */
    GROUP_CONCAT(
        DISTINCT CASE
            WHEN ce.remark != 'Resent' THEN ce.center_id
        END
        ORDER BY ce.center_id
    ) AS centers_escalated,

    /* Case type - Critical if Sent_again, Delayed, or Rejected_by_all */
    CASE
        WHEN SUM(ce.remark = 'Sent_again' AND ce.remark != 'Resent') > 0 THEN 'Critical'
        WHEN SUM(ce.remark = 'Delayed' AND ce.remark != 'Resent') > 0 THEN 'Critical'
        WHEN SUM(ce.remark = 'Rejected_by_all' AND ce.remark != 'Resent') > 0 THEN 'Critical'
        ELSE 'Standard'
    END AS case_type,

    /* Highest priority remark */
    CASE
        WHEN SUM(ce.remark = 'Sent_again' AND ce.remark != 'Resent') > 0 THEN 'Sent_again'
        WHEN SUM(ce.remark = 'Rejected_by_all' AND ce.remark != 'Resent') > 0 THEN 'Rejected_by_all'
        WHEN SUM(ce.remark = 'Delayed' AND ce.remark != 'Resent') > 0 THEN 'Delayed'
        ELSE 'None'
    END AS remark

FROM cases c
LEFT JOIN case_escalations ce
    ON c.case_id = ce.case_id

WHERE c.status = 'Pending'

GROUP BY c.case_id
ORDER BY 
    CASE WHEN SUM(ce.remark IN ('Sent_again', 'Delayed', 'Rejected_by_all')) > 0 THEN 0 ELSE 1 END,
    c.created_time DESC
";

$stmt = $conn->prepare($sql);
$stmt->execute();
$result = $stmt->get_result();

$critical_cases = [];
$standard_cases = [];

while ($row = $result->fetch_assoc()) {
    $case = [
        "case_id" => (int)$row['case_id'],
        "user_id" => (int)$row['user_id'],
        "type_of_animal" => $row['type_of_animal'],
        "animal_condition" => $row['animal_condition'],
        "photo" => $row['photo'],
        "case_type" => $row['case_type'],
        "case_status" => $row['case_status'],
        "latitude" => $row['latitude'],
        "longitude" => $row['longitude'],
        "created_time" => $row['created_time'],
        "case_age_minutes" => (int)$row['case_age_minutes'],
        "centers_escalated" => $row['centers_escalated']
            ? array_map('intval', explode(',', $row['centers_escalated']))
            : [],
        "remark" => $row['remark']
    ];

    if ($row['case_type'] === 'Critical') {
        $critical_cases[] = $case;
    } else {
        $standard_cases[] = $case;
    }
}

echo json_encode([
    "success" => true,
    "total_pending" => count($critical_cases) + count($standard_cases),
    "critical_count" => count($critical_cases),
    "standard_count" => count($standard_cases),
    "critical_cases" => $critical_cases,
    "standard_cases" => $standard_cases
]);
?>
