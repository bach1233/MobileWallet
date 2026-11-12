const { onCall, HttpsError } = require("firebase-functions/v2/https");
const { initializeApp } = require("firebase-admin/app");
const { getFirestore, FieldValue } = require("firebase-admin/firestore");

initializeApp();
const db = getFirestore();
const LAB_PIN = "2468";

function readTransfer(data) {
  const recipientUid = typeof data?.recipientUid === "string" ? data.recipientUid : "";
  const amount = Number.isInteger(data?.amount) ? data.amount : Number(data?.amount);
  const pin = typeof data?.pin === "string" ? data.pin : "";
  const message = typeof data?.message === "string" ? data.message.slice(0, 140) : "Lab transfer";

  if (!recipientUid || !Number.isSafeInteger(amount) || amount <= 0) {
    throw new HttpsError("invalid-argument", "Invalid transfer data");
  }
  return { recipientUid, amount, pin, message };
}

async function loadWallets(senderUid, recipientUid) {
  if (senderUid === recipientUid) {
    throw new HttpsError("invalid-argument", "Self transfer is not allowed");
  }
  const senderRef = db.collection("wallets").doc(senderUid);
  const recipientRef = db.collection("wallets").doc(recipientUid);
  return { senderRef, recipientRef };
}

function writeTransfer(tx, senderRef, recipientRef, sender, recipient, amount, message) {
  const senderTransaction = senderRef.collection("transactions").doc();
  const recipientTransaction = recipientRef.collection("transactions").doc();
  const timestamp = FieldValue.serverTimestamp();

  tx.update(senderRef, { balance: sender.balance - amount });
  tx.update(recipientRef, { balance: recipient.balance + amount });
  tx.set(senderTransaction, {
    type: `Chuyển: ${message}`,
    amount,
    timestamp
  });
  tx.set(recipientTransaction, {
    type: `Nhận: ${message}`,
    amount,
    timestamp
  });

  return senderTransaction.id;
}

// Deliberately weak backend for the vulnerable build. It authenticates the
// Firebase user and performs an atomic write, but does not enforce PIN,
// Premium/Admin role, transfer limit, or sender balance policy.
exports.createTransferVulnerable = onCall(async (request) => {
  if (!request.auth) throw new HttpsError("unauthenticated", "Login required");
  const { recipientUid, amount, message } = readTransfer(request.data);
  const { senderRef, recipientRef } = await loadWallets(request.auth.uid, recipientUid);

  return db.runTransaction(async (tx) => {
    const senderSnapshot = await tx.get(senderRef);
    const recipientSnapshot = await tx.get(recipientRef);
    if (!senderSnapshot.exists || !recipientSnapshot.exists) {
      throw new HttpsError("not-found", "Wallet not found");
    }
    const sender = senderSnapshot.data();
    const recipient = recipientSnapshot.data();
    const senderBalance = Number(sender.balance || 0);
    const recipientBalance = Number(recipient.balance || 0);
    const id = writeTransfer(
      tx,
      senderRef,
      recipientRef,
      { balance: senderBalance },
      { balance: recipientBalance },
      amount,
      message
    );
    return { ok: true, id, mode: "vulnerable" };
  });
});

// Hardened backend. All authorization decisions are made from server-side
// wallet data; the client-provided role, balance, PIN result, and amount
// limit are never trusted.
exports.createTransfer = onCall(async (request) => {
  if (!request.auth) throw new HttpsError("unauthenticated", "Login required");
  const { recipientUid, amount, pin, message } = readTransfer(request.data);
  const { senderRef, recipientRef } = await loadWallets(request.auth.uid, recipientUid);

  return db.runTransaction(async (tx) => {
    const senderSnapshot = await tx.get(senderRef);
    const recipientSnapshot = await tx.get(recipientRef);
    if (!senderSnapshot.exists || !recipientSnapshot.exists) {
      throw new HttpsError("not-found", "Wallet not found");
    }

    const sender = senderSnapshot.data();
    const recipient = recipientSnapshot.data();
    const senderBalance = Number(sender.balance || 0);
    const recipientBalance = Number(recipient.balance || 0);
    const isAdmin = sender.username === "admin" && sender.admin === true;
    const isPremium = sender.premium === true;
    const limit = isAdmin ? 50_000_000 : isPremium ? 10_000_000 : 2_000_000;

    if (pin !== LAB_PIN) throw new HttpsError("permission-denied", "Invalid transaction PIN");
    if (amount > limit) throw new HttpsError("permission-denied", "Transfer limit exceeded");
    if (amount > senderBalance) throw new HttpsError("failed-precondition", "Insufficient balance");

    const id = writeTransfer(
      tx,
      senderRef,
      recipientRef,
      { balance: senderBalance },
      { balance: recipientBalance },
      amount,
      message
    );
    return { ok: true, id, mode: "hardened" };
  });
});
