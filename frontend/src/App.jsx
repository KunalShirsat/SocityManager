import { useState } from "react";

const initialForm = {
  apartmentNumber: "",
  residentName: "",
  email: "",
  amount: "",
  description: ""
};

export default function App() {
  const [formData, setFormData] = useState(initialForm);
  const [status, setStatus] = useState(null);
  const [loading, setLoading] = useState(false);

  const handleChange = (event) => {
    const { name, value } = event.target;
    setFormData((prev) => ({ ...prev, [name]: value }));
  };

  const handleSubmit = async (event) => {
    event.preventDefault();
    setLoading(true);
    setStatus(null);

    try {
      const response = await fetch("http://localhost:8080/api/expenses", {
        method: "POST",
        headers: {
          "Content-Type": "application/json"
        },
        body: JSON.stringify({
          ...formData,
          amount: Number(formData.amount)
        })
      });

      if (!response.ok) {
        throw new Error("Failed to submit expense");
      }

      const data = await response.json();
      setStatus({ type: "success", message: `Receipt sent: ${data.receiptNumber}` });
      setFormData(initialForm);
    } catch (error) {
      setStatus({ type: "error", message: "Unable to submit. Please check the backend." });
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="page">
      <header>
        <h1>Society Manager</h1>
        <p>Track apartment maintenance payments and email receipts.</p>
      </header>

      <main>
        <form className="card" onSubmit={handleSubmit}>
          <div className="grid">
            <label>
              Apartment Number
              <input
                name="apartmentNumber"
                value={formData.apartmentNumber}
                onChange={handleChange}
                placeholder="A-203"
                required
              />
            </label>
            <label>
              Resident Name
              <input
                name="residentName"
                value={formData.residentName}
                onChange={handleChange}
                placeholder="Priya Patel"
                required
              />
            </label>
            <label>
              Email
              <input
                type="email"
                name="email"
                value={formData.email}
                onChange={handleChange}
                placeholder="resident@example.com"
                required
              />
            </label>
            <label>
              Amount
              <input
                type="number"
                min="0"
                step="0.01"
                name="amount"
                value={formData.amount}
                onChange={handleChange}
                placeholder="2500"
                required
              />
            </label>
          </div>
          <label>
            Description
            <textarea
              name="description"
              value={formData.description}
              onChange={handleChange}
              placeholder="Monthly maintenance - April"
              rows="3"
              required
            />
          </label>
          <button type="submit" disabled={loading}>
            {loading ? "Sending..." : "Record Payment & Email Receipt"}
          </button>
          {status && (
            <p className={`status ${status.type}`}>{status.message}</p>
          )}
        </form>
      </main>
    </div>
  );
}
