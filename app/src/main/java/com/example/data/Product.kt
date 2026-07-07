package com.example.data

data class Product(
    val id: String,
    val name: String,
    val subtitle: String,
    val price: Double,
    val originalPrice: Double,
    val tag: String?,
    val description: String,
    val features: List<String>,
    val specs: Map<String, String>,
    val highlightColor: String
)

data class BlogPost(
    val id: String,
    val title: String,
    val date: String,
    val category: String,
    val excerpt: String,
    val body: String,
    val readTime: String = "4 min read"
)

object BuzzShieldData {
    val products = listOf(
        Product(
            id = "classic",
            name = "BuzzShield Classic v2",
            subtitle = "Eco-Friendly UV Photon Technology",
            price = 999.0,
            originalPrice = 1999.0,
            tag = "BEST SELLER",
            description = "Engineered specifically for Indian homes, the BuzzShield Classic Zapper v2 uses safe, eco-friendly 365nm UV waves to attract mosquitoes and quietly zap them. Perfect for bedrooms, living rooms, and study tables. Family-safe, chemical-free protection.",
            features = listOf(
                "Safe for kids & pets (Shielded physical barrier)",
                "Chemical-free & odorless (No toxic sprays)",
                "Ultra-low power consumption (5W energy saving)",
                "Whisper-quiet operation (<20dB silent grid)"
            ),
            specs = mapOf(
                "Coverage Area" to "Up to 400 sq. ft.",
                "UV Wavelength" to "365 nm Phototactic Light",
                "Power Input" to "5V USB (Cable included)",
                "Grid Voltage" to "1200V Safe Shock Grid",
                "Weight" to "420g Portable Design",
                "Material" to "ABS Fireproof Protective Shell"
            ),
            highlightColor = "ElectricLime"
        ),
        Product(
            id = "pro",
            name = "BuzzShield Pro Heavy-Duty",
            subtitle = "Dual UV Tube & Active Cyclone Suction",
            price = 1799.0,
            originalPrice = 3499.0,
            tag = "MAX COVERAGE",
            description = "The heavy-duty defense system for larger living areas, balconies, patios, and independent houses. Features twin high-intensity UV lamps paired with a powerful, whisper-quiet suction fan that pulls mosquitoes into an escape-proof tray while the dual 2000V grid handles the rest.",
            features = listOf(
                "High-power dual-grid (2000V instant zapping)",
                "Active cyclone suction fan technology",
                "Detachable escape-proof collection tray",
                "Rugged rainproof housing for semi-outdoor use"
            ),
            specs = mapOf(
                "Coverage Area" to "Up to 1000 sq. ft. (Large Halls)",
                "UV Wavelength" to "365 nm - 395 nm Dual Band",
                "Power Input" to "Direct AC 220-240V plug",
                "Grid Voltage" to "2000V High Power Grid",
                "Weight" to "1.2 kg Sturdy Base",
                "Material" to "Industrial Grade Polycarbonate"
            ),
            highlightColor = "ElectricLime"
        ),
        Product(
            id = "mini",
            name = "BuzzShield Mini USB Go",
            subtitle = "Compact Travel & Outdoor Companion",
            price = 499.0,
            originalPrice = 999.0,
            tag = "PORTABLE",
            description = "Your personal shield against mosquitoes during monsoons, whether you are camping, traveling, studying, or relaxing on the patio. Powered by any standard USB power bank, laptop, or phone charger. Compact, lightweight, and incredibly effective.",
            features = listOf(
                "USB plug & play (Power bank compatible)",
                "Highly portable & ultra-lightweight",
                "Convenient integrated silicon hanging loop",
                "Safe 1000V low-amperage grid"
            ),
            specs = mapOf(
                "Coverage Area" to "Up to 150 sq. ft. (Personal Space)",
                "UV Wavelength" to "368 nm Personal Aura",
                "Power Input" to "5V Micro-USB Port",
                "Power Rating" to "2.5W Eco-efficiency",
                "Dimensions" to "12cm x 8.5cm x 8.5cm",
                "Weight" to "180g Ultra-light"
            ),
            highlightColor = "ElectricLime"
        )
    )

    val blogPosts = listOf(
        BlogPost(
            id = "monsoon-alert",
            title = "Monsoon Mosquito Alert: 5 Steps to Protect Your Living Room from Dengue",
            date = "July 5, 2026",
            category = "Monsoon Safety",
            excerpt = "With monsoons setting in, mosquito breeding is at an all-time high. Read how you can secure your family from deadly dengue carrying Aedes mosquitoes.",
            body = "Monsoon brings joy, lush greenery, and relief from scorching heat, but it also opens the floodgates for mosquito-borne illnesses like dengue, malaria, and chikungunya. The Aedes aegypti mosquito, which transmits dengue, breeds in stagnant clean water inside or near our homes.\n\nHere are 5 critical steps to shield your living room this monsoon:\n\n1. Eliminate Standing Water: Clean your flower vases, cooler trays, and balcony drains weekly. Even a small cap of stagnant water can breed hundreds of larvae.\n\n2. Use Physical Barriers: Keep mesh screens on doors and windows closed during early mornings and evenings (their peak biting times).\n\n3. Switch on Your BuzzShield Lamp: UV photon technology attracts mosquitoes continuously without spreading toxic chemicals in your home.\n\n4. Wear Long-Sleeved Clothing: Light-colored, breathable, long-sleeved clothing adds a physical layer of defense.\n\n5. Keep Indoor Plants Clean: Stagnant water in indoor plant saucers is a major breeding ground. Clean them regularly."
        ),
        BlogPost(
            id = "chemical-free-living",
            title = "Chemical-Free Living: Why Coils and Liquid Vaporizers May Be Harming Your Kids",
            date = "July 2, 2026",
            category = "Family Health",
            excerpt = "Many Indian households rely on coils or liquid vaporizers. Discover why switching to electric UV mosquito killer lamps is safer for kids and seniors.",
            body = "For decades, Indian households have lit mosquito coils and plugged in liquid vaporizers. But what are we actually breathing in? Standard mosquito coils release smoke containing fine particulate matter, formaldehyde, and heavy metals. Breathing the smoke of one coil is equivalent to smoking 70-100 cigarettes!\n\nLiquid vaporizers release synthetic pyrethroid insecticides which can cause respiratory issues, allergies, and headaches—especially in babies, toddlers, and elderly parents.\n\nWhy Electric UV Lamps (BuzzShield) are the Safer Choice:\n\n- 100% Chemical-Free: Attracts with safe UV waves and zaps with a shielded voltage grid.\n\n- Zero Smoke, Zero Smell: Clean and odorless operation for peaceful sleeping environments.\n\n- Safe Touch Grids: Shielded enclosure prevents curious fingers or pets from touching active elements."
        ),
        BlogPost(
            id = "science-of-365nm",
            title = "The Science Behind 365nm UV Light: How BuzzShield Zaps Quietly",
            date = "June 28, 2026",
            category = "Technology",
            excerpt = "Ever wondered why mosquitoes fly straight into the BuzzShield lamp? Let's look at the photoreceptor science that drives eco-friendly pest control.",
            body = "Mosquitoes have advanced compound eyes with photoreceptors highly sensitive to ultraviolet light. Research shows that wavelengths between 365nm and 368nm are irresistible to night-flying mosquitoes and pests.\n\nWhen BuzzShield emits this precise 365nm wavelength using its high-efficiency photon LED, it triggers a phototactic reaction in mosquitoes, compelling them to fly toward the source.\n\nOnce they enter, they are instantly eliminated by our whisper-quiet high-voltage grid (1200V-2000V). No harmful chemicals, no foul-smelling sprays, just pure physics working to secure your sleep."
        ),
        BlogPost(
            id = "balcony-chai-setup",
            title = "Balcony & Patio Setup: Enjoying Your Evening Chai Without the Mosquito Buzz",
            date = "June 25, 2026",
            category = "Home Decor",
            excerpt = "Don't let mosquitoes ruin your favorite evening monsoon ritual. Learn the best way to position your zapper for maximum outdoor coverage.",
            body = "An evening cup of hot masala chai on the balcony while listening to rainfall is an unmatched monsoon vibe. But the moment you sit, the biting brigade arrives!\n\nHere is how to set up an outdoor defense using the BuzzShield Pro:\n\n1. Placement Height: Hang or place your BuzzShield at 3 to 5 feet off the ground—this matches the typical flying altitude of seeking mosquitoes.\n\n2. Position Ahead of Time: Turn on the zapper 30 minutes before you sit outside. Let it attract and clear out any nearby pests first.\n\n3. Keep in Shadow: UV zappers work best in darker spots where the 365nm glow is highly visible to mosquitoes."
        ),
        BlogPost(
            id = "pediatrician-guide",
            title = "Dengue Prevention: A Doctor's Guide to Keeping Toddlers Safe This Rainy Season",
            date = "June 20, 2026",
            category = "Pediatrics",
            excerpt = "Pediatricians warn of rising mosquito-borne diseases. Learn crucial daily habits to safeguard toddlers and newborns from mosquito bites.",
            body = "Toddlers and newborns are particularly vulnerable to mosquito bites because their immune systems are still developing, and they cannot easily swat away pests. Dengue fever in young children can escalate quickly into severe complications.\n\nOur recommended pediatrician safety checklist:\n\n1. Day-bite awareness: Remember that the dengue-carrying mosquito is highly active during daylight hours.\n\n2. Crib Netting: Always cover baby strollers and cribs with fine mesh mosquito nets.\n\n3. Safe Zapping: Place a BuzzShield Classic on a dresser nearby (well out of reach of children) to keep the air clear while they sleep.\n\n4. Skin-safe repellents: Use natural oil-based roll-ons only on clothing, never directly on infant skin."
        )
    )
}
