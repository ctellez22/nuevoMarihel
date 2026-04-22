#!/bin/zsh
# Script para descubrir impresoras Zebra en la red y mostrar su IP

echo "🔍 Buscando impresoras Zebra en tu red..."
echo ""

# Método 1: Usar arp-scan si está disponible
if command -v arp-scan &> /dev/null; then
    echo "Usando arp-scan para buscar dispositivos..."
    arp-scan --localnet 2>/dev/null | grep -i zebra
else
    # Método 2: Usar arp -a (más común en macOS)
    echo "Buscando en tabla ARP..."
    arp -a | grep -i zebra
fi

echo ""
echo "Si no ves tu impresora arriba, intenta estos pasos:"
echo ""
echo "1️⃣  Verifica que tu impresora está encendida y conectada a WiFi"
echo ""
echo "2️⃣  Accede al menú de la impresora:"
echo "   • Presiona el botón MENU en la impresora"
echo "   • Busca 'Network' o 'Network Settings'"
echo "   • Anota la dirección IP (ej: 192.168.1.100)"
echo ""
echo "3️⃣  Una vez que tengas la IP, verifica conectividad:"
echo "   nc -zv 192.168.1.100 9100"
echo ""
echo "4️⃣  Luego ejecuta con:"
echo "   export PRINTER_IP=192.168.1.100"
echo "   java -Dprinter.ip=192.168.1.100 -jar marihel.jar"
echo ""

