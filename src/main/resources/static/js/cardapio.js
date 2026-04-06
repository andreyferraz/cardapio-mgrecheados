const cart = new Map();
const CART_STORAGE_KEY = "mgrecheados:cart:v1";

const elements = {
    cartCount: document.getElementById("cartCount"),
    cartItems: document.getElementById("cartItems"),
    cartTotal: document.getElementById("cartTotal"),
    cartSheet: document.getElementById("cartSheet"),
    openCartBtn: document.getElementById("openCartBtn"),
    clearCartBtn: document.getElementById("clearCartBtn"),
    checkoutBtn: document.getElementById("whatsAppCheckout")
};

function parseCurrency(number) {
    return new Intl.NumberFormat("pt-BR", {
        style: "currency",
        currency: "BRL"
    }).format(number);
}

function toNumber(value) {
    if (typeof value === "number") {
        return value;
    }
    const parsed = Number(String(value).replace(",", "."));
    return Number.isFinite(parsed) ? parsed : 0;
}

function updateCartBadge() {
    const totalItems = [...cart.values()].reduce((sum, item) => sum + item.quantity, 0);
    elements.cartCount.textContent = String(totalItems);
}

function saveCartToStorage() {
    const items = [...cart.values()];
    localStorage.setItem(CART_STORAGE_KEY, JSON.stringify(items));
}

function loadCartFromStorage() {
    const stored = localStorage.getItem(CART_STORAGE_KEY);
    if (!stored) {
        return;
    }

    try {
        const items = JSON.parse(stored);
        if (!Array.isArray(items)) {
            return;
        }

        items.forEach(item => {
            if (!item || !item.id || !item.name) {
                return;
            }

            const quantity = Number(item.quantity);
            const price = toNumber(item.price);
            if (!Number.isFinite(quantity) || quantity <= 0 || price < 0) {
                return;
            }

            cart.set(String(item.id), {
                id: String(item.id),
                name: String(item.name),
                weight: String(item.weight || ""),
                price,
                quantity
            });
        });
    } catch (_) {
        localStorage.removeItem(CART_STORAGE_KEY);
    }
}

function renderCart() {
    if (cart.size === 0) {
        elements.cartItems.innerHTML = '<p class="cart-empty">Seu carrinho esta vazio.</p>';
        elements.cartTotal.textContent = parseCurrency(0);
        elements.checkoutBtn.removeAttribute("href");
        return;
    }

    const rows = [];
    let total = 0;

    cart.forEach(item => {
        const subtotal = item.quantity * item.price;
        total += subtotal;
        rows.push(`
            <article class="cart-item">
                <div>
                    <p class="cart-item-name">${item.name}</p>
                    <p class="cart-item-sub">${item.weight} • ${parseCurrency(item.price)} cada</p>
                    <p class="cart-item-sub">Subtotal: ${parseCurrency(subtotal)}</p>
                </div>
                <div class="qty-controls">
                    <button type="button" data-op="minus" data-id="${item.id}">-</button>
                    <span>${item.quantity}</span>
                    <button type="button" data-op="plus" data-id="${item.id}">+</button>
                </div>
            </article>
        `);
    });

    elements.cartItems.innerHTML = rows.join("");
    elements.cartTotal.textContent = parseCurrency(total);

    const number = elements.checkoutBtn.dataset.whatsapp;
    const prefix = elements.checkoutBtn.dataset.prefix || "Ola, gostaria de fazer o pedido:";
    const lines = [prefix, ""];

    cart.forEach(item => {
        lines.push(`- ${item.quantity}x ${item.name} (${item.weight})`);
    });

    lines.push("");
    lines.push(`Total: ${parseCurrency(total)}`);

    const message = encodeURIComponent(lines.join("\n"));
    elements.checkoutBtn.href = `https://wa.me/${number}?text=${message}`;
}

function addToCart(product) {
    const existing = cart.get(product.id);
    if (existing) {
        existing.quantity += 1;
    } else {
        cart.set(product.id, { ...product, quantity: 1 });
    }
    saveCartToStorage();
    updateCartBadge();
    renderCart();
    elements.cartSheet.classList.add("open");
}

function changeQuantity(id, op) {
    const item = cart.get(id);
    if (!item) {
        return;
    }

    if (op === "plus") {
        item.quantity += 1;
    } else {
        item.quantity -= 1;
    }

    if (item.quantity <= 0) {
        cart.delete(id);
    }

    saveCartToStorage();
    updateCartBadge();
    renderCart();
}

function bindBuyButtons() {
    document.querySelectorAll(".buy-btn").forEach(button => {
        button.addEventListener("click", () => {
            addToCart({
                id: button.dataset.id,
                name: button.dataset.nome,
                weight: button.dataset.peso,
                price: toNumber(button.dataset.preco)
            });
        });
    });
}

function bindCartEvents() {
    elements.openCartBtn.addEventListener("click", () => {
        elements.cartSheet.classList.toggle("open");
    });

    elements.clearCartBtn.addEventListener("click", () => {
        cart.clear();
        saveCartToStorage();
        updateCartBadge();
        renderCart();
    });

    elements.checkoutBtn.addEventListener("click", event => {
        if (cart.size === 0 || !elements.checkoutBtn.getAttribute("href")) {
            event.preventDefault();
        }
    });

    elements.cartItems.addEventListener("click", event => {
        const target = event.target;
        if (!(target instanceof HTMLElement)) {
            return;
        }
        const op = target.dataset.op;
        const id = target.dataset.id;
        if (op && id) {
            changeQuantity(id, op);
        }
    });
}

loadCartFromStorage();
bindBuyButtons();
bindCartEvents();
updateCartBadge();
renderCart();
