package id.ac.stmi.vertue

const val VERTUE_WA = "6288999999977"

data class CatalogItem(
    val category: String,
    val name: String,
    val desc: String,
    val price: String
)

object Catalog {
    val categories = listOf("Semua", "Jok", "Panel & Setir", "Plafon & Karpet", "Fitur & Custom", "Restorasi")

    val items = listOf(
        CatalogItem("Jok", "Retrim Jok Kulit Sintetis",
            "Pelapisan ulang jok dengan kulit sintetis buatan lokal.", "±Rp5 – 7 jt"),
        CatalogItem("Jok", "Retrim Jok Kulit Asli Impor",
            "Mobil 2 baris sekitar Rp10 jt, mobil 3 baris sekitar Rp13 jt.", "±Rp10 – 13 jt"),
        CatalogItem("Jok", "Pilihan Bahan Pelapis",
            "Semi, microfiber, autoleder, Wollsdorf, dan half leather.", "Rp4 – 10 jt"),
        CatalogItem("Jok", "Kulit Aircraft",
            "Tahan api hingga 900°C dan sekitar 30% lebih ringan dari kulit biasa.", "Konsultasi"),
        CatalogItem("Jok", "Busa Latex",
            "Jok lebih empuk, termasuk penyesuaian sudut sandaran belakang.", "Konsultasi"),

        CatalogItem("Panel & Setir", "Setir & Shift-Knob",
            "Perbaikan dan pelapisan ulang setir, shift-knob, dan covernya.", "Mulai ±Rp2 jt"),
        CatalogItem("Panel & Setir", "Dasbor & Panel Trimming",
            "Pelapisan dasbor dan panel trimming dengan kulit atau fabric.", "Mulai ±Rp2 jt"),
        CatalogItem("Panel & Setir", "Pilar A-B-C",
            "Pelapisan ulang pilar depan, tengah, dan belakang.", "Mulai ±Rp2 jt"),
        CatalogItem("Panel & Setir", "Door Trim",
            "Pelapisan dan perbaikan panel pintu, termasuk model vakum.", "Konsultasi"),

        CatalogItem("Plafon & Karpet", "Plafon",
            "Penggantian dan pelapisan ulang plafon.", "Konsultasi"),
        CatalogItem("Plafon & Karpet", "Karpet Dasar & Floor Mat",
            "Karpet dasar dan floor mat sesuai ukuran mobil.", "Konsultasi"),
        CatalogItem("Plafon & Karpet", "Gorden",
            "Gorden interior untuk kenyamanan penumpang.", "Konsultasi"),

        CatalogItem("Fitur & Custom", "Jok Elektrik",
            "Maju-mundur dan reclining elektrik. Contoh pengerjaan Innova Zenix. Bahan pelapis dipilih terpisah.", "±Rp34 jt"),
        CatalogItem("Fitur & Custom", "Pilot Seat",
            "Dilengkapi memory seat, pijat, wireless charging, ambient light, dan meja lipat.", "Konsultasi"),
        CatalogItem("Fitur & Custom", "Custom Bagasi & Konsol",
            "Pembuatan bagasi dan konsol tengah sesuai kebutuhan.", "Konsultasi"),
        CatalogItem("Fitur & Custom", "Interior Custom dari Nol",
            "Pengerjaan lengkap untuk mobil kosong: plafon, jok, door trim, dan karpet.", "Konsultasi"),

        CatalogItem("Restorasi", "Reparasi Jok & Door Trim",
            "Perbaikan kulit yang rusak. Elemen heater jok bisa terdampak, tetapi tidak memengaruhi elektrikal mobil.", "Konsultasi"),
        CatalogItem("Restorasi", "Restorasi Mobil Klasik",
            "Restorasi interior total dengan menjaga karakter asli mobil.", "Konsultasi")
    )
}