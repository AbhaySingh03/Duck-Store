'use strict';

const COLORS = ['Red', 'Green', 'Yellow', 'Black'];          // must match the backend
const SIZES = ['XLarge', 'Large', 'Medium', 'Small', 'XSmall'];

// ---------- API (the only place that talks HTTP) ----------
async function request(path, options = {}) {
  const response = await fetch(path, { headers: { 'Content-Type': 'application/json' }, ...options });
  if (response.status === 204) return { status: 204, data: null };
  let body = null;
  try { body = await response.json(); } catch (e) { /* empty or non-JSON body */ }
  if (!response.ok) {
    const details = body && body.details && body.details.length ? ' (' + body.details.join('; ') + ')' : '';
    throw new Error(((body && body.message) || 'Request failed with status ' + response.status) + details);
  }
  return { status: response.status, data: body };
}
const api = {
  list: async (sort) => (await request('/api/ducks?sort=' + sort)).data,
  add: async (duck) => {
    const r = await request('/api/ducks', { method: 'POST', body: JSON.stringify(duck) });
    return { duck: r.data, merged: r.status === 200 };           // 200 = merged into an existing duck
  },
  update: async (id, v) =>
    (await request('/api/ducks/' + id, { method: 'PUT', body: JSON.stringify({ price: v.price, quantity: v.quantity }) })).data,
  remove: (id) => request('/api/ducks/' + id, { method: 'DELETE' }),
};

// ---------- state + elements ----------
let sort = 'desc';
let editing = null;          // the duck being edited, or null when adding
let pendingDelete = null;

const $ = (id) => document.getElementById(id);
const el = { rows: $('rows'), empty: $('empty'), notice: $('notice'), pageError: $('page-error'),
  form: $('duck-form'), formTitle: $('form-title'), formHint: $('form-hint'), formError: $('form-error'),
  color: $('color'), size: $('size'), price: $('price'), quantity: $('quantity'),
  saveBtn: $('save-btn'), addBtn: $('add-btn'), sortBtn: $('sort-btn'),
  confirm: $('confirm'), confirmText: $('confirm-text') };

function show(node, text) { node.textContent = text || ''; node.hidden = !text; }

// ---------- list ----------
async function reload() {
  try {
    const ducks = await api.list(sort);
    renderRows(ducks);
    show(el.pageError, '');
  } catch (e) {
    show(el.pageError, 'Could not load ducks: ' + e.message);
  }
}

function renderRows(ducks) {
  el.rows.replaceChildren();
  el.empty.hidden = ducks.length > 0;
  el.sortBtn.textContent = 'Quantity ' + (sort === 'asc' ? '▲' : '▼');
  for (const duck of ducks) {
    const tr = document.createElement('tr');
    addCell(tr, duck.id);
    addCell(tr, duck.color);
    addCell(tr, duck.size);
    addCell(tr, duck.price.toFixed(2) + ' USD', 'num');
    addCell(tr, duck.quantity, 'num');

    const actions = document.createElement('td');
    actions.append(linkButton('Edit', '', () => openForm(duck)), ' / ',
                   linkButton('Delete', 'danger', () => askDelete(duck)));
    tr.append(actions);
    el.rows.append(tr);
  }
}
function addCell(tr, text, cls) {
  const td = document.createElement('td');
  td.textContent = text;                       // textContent: never interpret data as HTML
  if (cls) td.className = cls;
  tr.append(td);
}
function linkButton(label, extraClass, onClick) {
  const b = document.createElement('button');
  b.className = 'link ' + extraClass;
  b.textContent = label;
  b.addEventListener('click', onClick);
  return b;
}

// ---------- add / edit form ----------
function fillSelect(select, values) {
  for (const v of values) select.append(new Option(v, v));
}

function openForm(duck) {
  editing = duck || null;
  show(el.notice, '');
  show(el.formError, '');
  el.formTitle.textContent = editing ? 'Edit duck ' + editing.id : 'Add duck';
  el.saveBtn.textContent = editing ? 'Save changes' : 'Add duck';
  el.formHint.hidden = Boolean(editing);
  el.color.value = editing ? editing.color : COLORS[0];
  el.size.value = editing ? editing.size : SIZES[0];
  el.color.disabled = el.size.disabled = Boolean(editing);   // color and size are read-only when editing
  el.price.value = editing ? String(editing.price) : '';
  el.quantity.value = editing ? String(editing.quantity) : '';
  el.form.hidden = false;
  el.addBtn.disabled = true;
  (editing ? el.price : el.color).focus();
}

function closeForm() {
  el.form.hidden = true;
  el.addBtn.disabled = false;
  editing = null;
}

function validate(price, quantity) {
  if (!/^\d{1,8}(\.\d{1,2})?$/.test(price) || Number(price) <= 0) {
    return 'Price must be greater than 0 with at most 2 decimals.';
  }
  const min = editing ? 0 : 1;
  if (!/^\d+$/.test(quantity) || Number(quantity) < min || Number(quantity) > 1000000) {
    return 'Quantity must be a whole number between ' + min + ' and 1,000,000.';
  }
  return '';
}

async function submitForm(event) {
  event.preventDefault();
  const problem = validate(el.price.value.trim(), el.quantity.value.trim());
  if (problem) return show(el.formError, problem);

  const values = { color: el.color.value, size: el.size.value,
    price: Number(el.price.value), quantity: Number(el.quantity.value) };
  el.saveBtn.disabled = true;
  show(el.formError, '');
  try {
    if (editing) {
      await api.update(editing.id, values);
      show(el.notice, 'Duck ' + editing.id + ' updated.');
    } else {
      const { duck, merged } = await api.add(values);
      show(el.notice, merged
        ? 'Added to existing duck ' + duck.id + ': it now has ' + duck.quantity + ' in stock.'
        : 'Duck ' + duck.id + ' added.');
    }
    closeForm();
    await reload();
  } catch (e) {
    show(el.formError, e.message);
  } finally {
    el.saveBtn.disabled = false;
  }
}

// ---------- delete with confirmation ----------
function askDelete(duck) {
  pendingDelete = duck;
  el.confirmText.textContent = 'Delete the ' + duck.color + ' ' + duck.size + ' duck (id ' + duck.id + ', '
    + duck.quantity + ' in stock)?';
  el.confirm.showModal();
}

async function confirmDelete() {
  const duck = pendingDelete;
  el.confirm.close();
  try {
    await api.remove(duck.id);
    show(el.notice, 'Duck ' + duck.id + ' deleted.');
    await reload();
  } catch (e) {
    show(el.pageError, e.message);
  }
}

// ---------- wiring ----------
fillSelect(el.color, COLORS);
fillSelect(el.size, SIZES);
el.addBtn.addEventListener('click', () => openForm(null));
el.form.addEventListener('submit', submitForm);
$('cancel-btn').addEventListener('click', closeForm);
el.sortBtn.addEventListener('click', () => { sort = sort === 'asc' ? 'desc' : 'asc'; reload(); });
$('confirm-yes').addEventListener('click', confirmDelete);
$('confirm-no').addEventListener('click', () => el.confirm.close());
reload();
