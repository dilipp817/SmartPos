package com.autobill.smartpos.data.printer

/**
 * ESC/POS byte-level commands for generic 80 mm thermal printers
 * (Xprinter, GOOJPRT, TVS RP80, etc.).
 *
 * Reference: ESC/POS Command Manual (Epson-compatible subset).
 * All constants are ByteArray for direct append to an output stream.
 */
internal object EscPosCommands {

    // ── Printer control ───────────────────────────────────────────────────────

    /** ESC @ — initialize (reset) printer. Must be the first command sent. */
    val INIT: ByteArray = byteArrayOf(0x1B, 0x40)

    /** LF  — print and feed one line. */
    val LF: ByteArray = byteArrayOf(0x0A)

    /** ESC d n — feed n lines. */
    fun feedLines(n: Int): ByteArray = byteArrayOf(0x1B, 0x64, n.toByte())

    /**
     * GS V m n — partial cut with n-line feed before cutting.
     * m = 66 (0x42) = partial cut.
     */
    val PARTIAL_CUT: ByteArray = byteArrayOf(0x1D, 0x56, 0x42, 0x03)

    // ── Text alignment ────────────────────────────────────────────────────────

    /** ESC a 0 — left align. */
    val ALIGN_LEFT: ByteArray = byteArrayOf(0x1B, 0x61, 0x00)

    /** ESC a 1 — center align. */
    val ALIGN_CENTER: ByteArray = byteArrayOf(0x1B, 0x61, 0x01)

    // ── Text emphasis ─────────────────────────────────────────────────────────

    /** ESC E 1 — bold on. */
    val BOLD_ON: ByteArray = byteArrayOf(0x1B, 0x45, 0x01)

    /** ESC E 0 — bold off. */
    val BOLD_OFF: ByteArray = byteArrayOf(0x1B, 0x45, 0x00)

    /** ESC ! 0 — normal character size (cancel double-width/height). */
    val SIZE_NORMAL: ByteArray = byteArrayOf(0x1B, 0x21, 0x00)

    /** ESC ! 0x30 — double width + double height (for restaurant name header). */
    val SIZE_DOUBLE: ByteArray = byteArrayOf(0x1B, 0x21, 0x30)

    // ── Separator helpers ─────────────────────────────────────────────────────

    /** 48-char line of '=' characters (section divider). */
    val DIVIDER_HEAVY: ByteArray = "================================================".toByteArray(Charsets.UTF_8)

    /** 48-char line of '-' characters (column divider inside sections). */
    val DIVIDER_LIGHT: ByteArray = "------------------------------------------------".toByteArray(Charsets.UTF_8)
}

