package com.example.paperwallet

import android.content.Context
import android.graphics.*
import android.graphics.pdf.PdfDocument
import com.google.zxing.BarcodeFormat
import com.google.zxing.qrcode.QRCodeWriter
import java.io.File
import java.io.FileOutputStream

object WalletPdfGenerator {

    // 绝对固定坐标点 (A4纸尺寸 595x842)
    private const val ADDR_TEXT_X = 50f
    private const val ADDR_TEXT_Y = 80f
    private const val ADDR_QR_X = 50f
    private const val ADDR_QR_Y = 100f

    private const val PK_TEXT_X = 320f
    private const val PK_TEXT_Y = 80f
    private const val PK_QR_X = 320f
    private const val PK_QR_Y = 100f

    private const val QR_SIZE = 220

    fun generatePdf(context: Context, chain: String): File {
        val (address, privateKey) = CryptoUtils.generateAddressAndKey(chain)

        val pdfDocument = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create()
        val page = pdfDocument.startPage(pageInfo)
        val canvas = page.canvas

        try {
            val bgName = "${chain.lowercase()}.jpg"
            val bgStream = context.assets.open("backgrounds/$bgName")
            val bgBitmap = BitmapFactory.decodeStream(bgStream)
            val scaledBg = Bitmap.createScaledBitmap(bgBitmap, 595, 842, true)
            canvas.drawBitmap(scaledBg, 0f, 0f, null)
            bgStream.close()
        } catch (e: Exception) {
            canvas.drawColor(Color.WHITE)
        }

        val paint = Paint().apply {
            color = Color.BLACK
            textSize = 11f
            isAntiAlias = true
            typeface = Typeface.MONOSPACE
        }

        canvas.drawText("Chain: $chain", ADDR_TEXT_X, ADDR_TEXT_Y - 20, paint)
        canvas.drawText("Addr: $address", ADDR_TEXT_X, ADDR_TEXT_Y, paint)
        
        canvas.drawText("PrivKey:", PK_TEXT_X, PK_TEXT_Y, paint)
        canvas.drawText(privateKey.take(32), PK_TEXT_X, PK_TEXT_Y + 15, paint)
        canvas.drawText(privateKey.drop(32), PK_TEXT_X, PK_TEXT_Y + 30, paint)

        canvas.drawBitmap(generateQr(address), ADDR_QR_X, ADDR_QR_Y, null)
        canvas.drawBitmap(generateQr(privateKey), PK_QR_X, PK_QR_Y, null)

        pdfDocument.finishPage(page)

        val dir = context.getExternalFilesDir(null) ?: context.filesDir
        val file = File(dir, "paper_wallet_${chain}_${System.currentTimeMillis()}.pdf")
        pdfDocument.writeTo(FileOutputStream(file))
        pdfDocument.close()

        return file
    }

    private fun generateQr(content: String): Bitmap {
        val bitMatrix = QRCodeWriter().encode(content, BarcodeFormat.QR_CODE, QR_SIZE, QR_SIZE)
        val bitmap = Bitmap.createBitmap(QR_SIZE, QR_SIZE, Bitmap.Config.ARGB_8888)
        for (x in 0 until QR_SIZE) {
            for (y in 0 until QR_SIZE) {
                bitmap.setPixel(x, y, if (bitMatrix[x, y]) Color.BLACK else Color.WHITE)
            }
        }
        return bitmap
    }
}