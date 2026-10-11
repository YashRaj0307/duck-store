import { useCallback, useEffect, useMemo, useState } from "react";

const API_BASE = (import.meta.env.VITE_API_BASE_URL || "http://localhost:8080").replace(/\/$/, "");
const COLORS = ["Red", "Green", "Yellow", "Black"];
const SIZES = ["XLarge", "Large", "Medium", "Small", "XSmall"];
const EMPTY_FORM = { color: "Yellow", size: "Medium", price: "9.99", quantity: "10" };

async function api(path, options = {}) {
  const response = await fetch(`${API_BASE}${path}`, {
    ...options,
    headers: { "Content-Type": "application/json", ...options.headers },
  });

  if (response.status === 204) return null;
  const body = await response.json().catch(() => ({}));
  if (!response.ok) {
    const fields = body.fieldErrors
      ? Object.values(body.fieldErrors).join(" ")
      : "";
    throw new Error(fields || body.detail || body.message || `Request failed (${response.status})`);
  }
  return body;
}

function money(value) {
  return new Intl.NumberFormat("en-US", {
    style: "currency",
    currency: "USD",
  }).format(Number(value));
}

export default function App() {
  const [ducks, setDucks] = useState([]);
  const [form, setForm] = useState(EMPTY_FORM);
  const [editing, setEditing] = useState(null);
  const [loading, setLoading] = useState(true);
  const [saving, setSaving] = useState(false);
  const [error, setError] = useState("");
  const [notice, setNotice] = useState("");

  const totalUnits = useMemo(
    () => ducks.reduce((sum, duck) => sum + duck.quantity, 0),
    [ducks]
  );

  const loadDucks = useCallback(async () => {
    setLoading(true);
    try {
      setDucks(await api("/api/ducks"));
      setError("");
    } catch (err) {
      setError(err.message || "Could not load inventory.");
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => {
    loadDucks();
  }, [loadDucks]);

  function updateForm(event) {
    setForm((current) => ({ ...current, [event.target.name]: event.target.value }));
  }

  function startEdit(duck) {
    setEditing(duck);
    setForm({ price: String(duck.price), quantity: String(duck.quantity) });
    setError("");
    setNotice("");
  }

  function cancelEdit() {
    setEditing(null);
    setForm(EMPTY_FORM);
  }

  async function submitForm(event) {
    event.preventDefault();
    setSaving(true);
    setError("");
    setNotice("");
    try {
      if (editing) {
        await api(`/api/ducks/${editing.id}`, {
          method: "PUT",
          body: JSON.stringify({
            price: Number(form.price),
            quantity: Number(form.quantity),
          }),
        });
        setNotice(`Duck #${editing.id} updated.`);
      } else {
        await api("/api/ducks", {
          method: "POST",
          body: JSON.stringify({
            color: form.color,
            size: form.size,
            price: Number(form.price),
            quantity: Number(form.quantity),
          }),
        });
        setNotice("Inventory saved. Matching active ducks are merged automatically.");
      }
      cancelEdit();
      await loadDucks();
    } catch (err) {
      setError(err.message || "Could not save this change.");
    } finally {
      setSaving(false);
    }
  }

  async function deleteDuck(duck) {
    if (!window.confirm(`Soft-delete ${duck.color} ${duck.size} duck #${duck.id}?`)) return;
    setError("");
    setNotice("");
    try {
      await api(`/api/ducks/${duck.id}`, { method: "DELETE" });
      setNotice(`Duck #${duck.id} was removed from active inventory.`);
      await loadDucks();
    } catch (err) {
      setError(err.message || "Could not delete this duck.");
    }
  }

  return (
    <main className="page-shell">
      <header className="topbar">
        <a className="brand" href="/" aria-label="Duck Store home">
          <span className="brand-mark" aria-hidden="true">D</span>
          <span>duck<span className="brand-light">store</span></span>
        </a>
        <span className="environment"><span className="status-dot" /> Warehouse</span>
      </header>

      <section className="intro">
        <div>
          <p className="eyebrow">INVENTORY MANAGEMENT</p>
          <h1>Duck warehouse</h1>
          <p className="subtitle">Manage stock, keep quantities accurate, and maintain a clean inventory.</p>
        </div>
        <div className="summary-card">
          <span className="summary-label">Active product types</span>
          <strong>{ducks.length}</strong>
          <span className="summary-foot">{totalUnits.toLocaleString()} units in stock</span>
        </div>
      </section>

      {error && <div className="alert alert-error" role="alert"><span>!</span><div>{error}</div><button onClick={() => setError("")} aria-label="Dismiss error">×</button></div>}
      {notice && <div className="alert alert-success" role="status"><span>✓</span><div>{notice}</div><button onClick={() => setNotice("")} aria-label="Dismiss message">×</button></div>}

      <section className="workspace">
        <aside className="panel form-panel">
          <div className="panel-heading">
            <div>
              <p className="eyebrow">{editing ? "UPDATE INVENTORY" : "NEW INVENTORY"}</p>
              <h2>{editing ? `Edit duck #${editing.id}` : "Add ducks"}</h2>
            </div>
            <span className="heading-icon" aria-hidden="true">{editing ? "↗" : "+"}</span>
          </div>

          <form onSubmit={submitForm} className="duck-form">
            {!editing && (
              <>
                <label>
                  Color
                  <select name="color" value={form.color} onChange={updateForm}>
                    {COLORS.map((color) => <option key={color}>{color}</option>)}
                  </select>
                </label>
                <label>
                  Size
                  <select name="size" value={form.size} onChange={updateForm}>
                    {SIZES.map((size) => <option key={size}>{size}</option>)}
                  </select>
                </label>
              </>
            )}
            {editing && (
              <div className="read-only-fields">
                <div><span>Color</span><strong>{editing.color}</strong></div>
                <div><span>Size</span><strong>{editing.size}</strong></div>
                <p>Color and size cannot be changed after a duck is created.</p>
              </div>
            )}
            <label>
              Unit price <span className="field-hint">USD</span>
              <div className="input-prefix"><span>$</span><input name="price" type="number" min="0.01" max="9999999999.99" step="0.01" value={form.price} onChange={updateForm} required /></div>
            </label>
            <label>
              Quantity
              <input name="quantity" type="number" min="1" max="2147483647" step="1" value={form.quantity} onChange={updateForm} required />
            </label>
            <p className="form-note">
              {editing
                ? "Only price and quantity are editable."
                : "If the same color, size, and price already exist, the new quantity is added to the existing stock."}
            </p>
            <button className="primary-button" type="submit" disabled={saving}>
              {saving ? "Saving…" : editing ? "Save changes" : "Add to warehouse"}
              {!saving && <span aria-hidden="true">→</span>}
            </button>
            {editing && <button className="secondary-button" type="button" onClick={cancelEdit}>Cancel editing</button>}
          </form>
        </aside>

        <section className="panel inventory-panel">
          <div className="panel-heading inventory-heading">
            <div>
              <p className="eyebrow">STOCK OVERVIEW</p>
              <h2>Current inventory <span className="count-pill">{ducks.length}</span></h2>
            </div>
            <button className="refresh-button" type="button" onClick={loadDucks} disabled={loading} title="Refresh inventory">↻ <span>Refresh</span></button>
          </div>
          <div className="table-wrap">
            <table>
              <thead>
                <tr><th>Product</th><th>Size</th><th>Unit price</th><th>Quantity</th><th><span className="sr-only">Actions</span></th></tr>
              </thead>
              <tbody>
                {loading ? (
                  <tr><td colSpan="5" className="empty-state"><span className="spinner" />Loading inventory…</td></tr>
                ) : ducks.length === 0 ? (
                  <tr><td colSpan="5" className="empty-state"><span className="empty-emoji">◌</span><strong>No active ducks yet</strong><span>Add your first duck using the form.</span></td></tr>
                ) : ducks.map((duck) => (
                  <tr key={duck.id}>
                    <td>
                      <div className="product-cell">
                        <span className={`duck-swatch swatch-${duck.color.toLowerCase()}`} aria-hidden="true">●</span>
                        <div><strong>{duck.color} duck</strong><span>SKU #{duck.id}</span></div>
                      </div>
                    </td>
                    <td><span className="size-chip">{duck.size}</span></td>
                    <td className="price-cell">{money(duck.price)}</td>
                    <td><span className={`quantity-chip ${duck.quantity < 10 ? "quantity-low" : ""}`}>{duck.quantity.toLocaleString()}</span></td>
                    <td>
                      <div className="row-actions">
                        <button type="button" onClick={() => startEdit(duck)} aria-label={`Edit duck ${duck.id}`} title="Edit">Edit</button>
                        <button type="button" className="delete-action" onClick={() => deleteDuck(duck)} aria-label={`Delete duck ${duck.id}`} title="Delete">Delete</button>
                      </div>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
          <div className="table-footer">
            <span>Sorted by quantity · lowest first</span>
            <span>{ducks.length} active {ducks.length === 1 ? "record" : "records"}</span>
          </div>
        </section>
      </section>
      <footer className="footer">
        <span>Duck Store</span><span>Warehouse inventory · API connected to {API_BASE}</span>
      </footer>
    </main>
  );
}
