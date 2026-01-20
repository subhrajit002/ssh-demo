import { useState } from "react";

async function fetchTooltip(productId, locationId, term) {
  const res = await fetch(
    `http://localhost:8080/api/audit/latest?productId=${productId}&locationId=${locationId}&term=${term}&whereCondition=4`,
  );
  return res.json();
}   

export default function ReviewForecast() {
  const [tooltip, setTooltip] = useState(null);

  var term = "term1"

  const handleHover = async () => {
    const data = await fetchTooltip("P1", "L1", term);
    setTooltip(data);
  };

  const override = async () => {
    await fetch("http://localhost:8080/api/override", {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({
        productId: "P1",
        locationId: "L1",
        term: term,
        newValue: 20,
        module: "DP",
        whereCondition: 4,
        component: "ReviewForecastService",
        userId: "userA",
      }),
    });
  };

  return (
    <div>
      <h2>Review Forecast</h2>

      <div onMouseEnter={handleHover}>
        Term5 Value
        {tooltip && (
          <div>
            {tooltip.values_before} → {tooltip.values_after}
            <br />
            {tooltip.timestamp}
            <br />
            {tooltip.user_id}
          </div>
        )}
      </div>

      <button onClick={override}>Override to 20</button>
    </div>
  );
}


export default function Worksheet() {
  const override = async () => {
    await fetch("http://localhost:8080/api/override", {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({
        productId: "P1",
        locationId: "L1",
        term: "term1",
        newValue: 5,
        module: "DP",
        whereCondition: 4,
        component: "WorksheetService",
        userId: "userB",
      }),
    });
  };

  return (
    <div>
      <h2>Worksheet</h2>
      <button onClick={override}>Override to 15</button>
    </div>
  );
}



// Tooltip API
    @GetMapping("/audit/latest")
    public Map<String, Object> getLatestAudit(
            @RequestParam String productId,
            @RequestParam String locationId,
            @RequestParam String term,
            @RequestParam int whereCondition) {
        return overrideService.getLatestAudit(
                productId,
                locationId,
                term,
                whereCondition);
    }

public Map<String, Object> getLatestAudit(
            String productId,
            String locationId,
            String termColumn,
            int whereCondition) {
        String sql = "SELECT values_before, values_after, timestamp, user_id " +
                "FROM dp_audit_logs " +
                "WHERE table_name='dp_historic_sales' " +
                "AND column_name=? " +
                "AND product_id=? " +
                "AND location_id=? " +
                "AND where_condition=? " +
                "ORDER BY timestamp DESC " +
                "LIMIT 1";

        return jdbcTemplate.queryForMap(
                sql,
                termColumn,
                productId,
                locationId,
                whereCondition);
    }
