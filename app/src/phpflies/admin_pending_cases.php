<?php
header("Content-Type: application/json");
include("db.php");

/*
|--------------------------------------------------------------------------
| ADMIN – PENDING CASES
|--------------------------------------------------------------------------
| Get cases from cases table where status = 'Reported' (not yet accepted)
| Join case_escalations to determine Critical/Standard:
|   - Sent_again remark = Critical
|   - None/other remark = Standard
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
    c.status,

    /* Get remark from case_escalations (prioritize Sent_again) */
    (SELECT 
        CASE 
            WHEN MAX(ce.remark = 'Sent_again') = 1 THEN 'Sent_again'
            ELSE 'None'
        END
     FROM case_escalations ce 
     WHERE ce.case_id = c.case_id
    ) AS remark,

    /* Get center_ids this case was escalated to */
    (SELECT GROUP_CONCAT(DISTINCT ce2.center_id ORDER BY ce2.center_id)
     FROM case_escalations ce2 
     WHERE ce2.case_id = c.case_id
    ) AS centers_escalated,

    /* Calculate case age in minutes */
    TIMESTAMPDIFF(MINUTE, c.created_time, NOW()) AS case_age_minutes

FROM cases c
WHERE c.status = 'Reported'
ORDER BY c.created_time DESC
";

$result = $conn->query($sql);

$critical_cases = [];
$standard_cases = [];

if ($result) {
    while ($row = $result->fetch_assoc()) {
        $case = [
            "case_id" => (int)$row['case_id'],
            "user_id" => (int)$row['user_id'],
            "type_of_animal" => $row['type_of_animal'],
            "animal_condition" => $row['animal_condition'],
            "photo" => $row['photo'],
            "latitude" => $row['latitude'],
            "longitude" => $row['longitude'],
            "created_time" => $row['created_time'],
            "case_age_minutes" => (int)$row['case_age_minutes'],
            "remark" => $row['remark'],
            "case_type" => ($row['remark'] === 'Sent_again') ? 'Critical' : 'Standard',
            "centers_escalated" => $row['centers_escalated']
                ? array_map('intval', explode(',', $row['centers_escalated']))
                : []
        ];

        if ($row['remark'] === 'Sent_again') {
            $critical_cases[] = $case;
        } else {
            $standard_cases[] = $case;
        }
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
