#!/bin/bash
# Quick BungeeCord test setup script

echo "Setting up BungeeCord test environment..."

# Create directories
mkdir -p bungeecord
mkdir -p server1
mkdir -p server2

# Download BungeeCord (Waterfall)
echo "Downloading Waterfall..."
wget -O bungeecord/waterfall.jar https://api.papermc.io/v2/projects/waterfall/versions/1.21/builds/574/downloads/waterfall-1.21-574.jar

# Download Paper for backend servers
echo "Downloading Paper for backend servers..."
wget -O server1/paper.jar https://api.papermc.io/v2/projects/paper/versions/1.21.6/builds/180/downloads/paper-1.21.6-180.jar
cp server1/paper.jar server2/paper.jar

# Create BungeeCord config
cat > bungeecord/config.yml << 'EOF'
ip_forward: true
network_compression_threshold: 256
permissions:
  default:
  - bungeecord.command.server
  - bungeecord.command.list
  admin:
  - bungeecord.command.alert
  - bungeecord.command.end
  - bungeecord.command.ip
  - bungeecord.command.reload
servers:
  lobby:
    motd: '&1Lobby Server'
    address: localhost:25566
    restricted: false
  survival:
    motd: '&1Survival Server'
    address: localhost:25567
    restricted: false
listeners:
- host: 0.0.0.0:25577
  max_players: 100
  tab_size: 60
  force_default_server: false
  priorities:
  - lobby
EOF

# Create server configs
echo "eula=true" > server1/eula.txt
echo "eula=true" > server2/eula.txt

# Server 1 properties
cat > server1/server.properties << 'EOF'
server-port=25566
online-mode=false
motd=Lobby Server
EOF

# Server 2 properties  
cat > server2/server.properties << 'EOF'
server-port=25567
online-mode=false
motd=Survival Server
EOF

# Create spigot.yml for both servers
for dir in server1 server2; do
cat > $dir/spigot.yml << 'EOF'
settings:
  bungeecord: true
EOF
done

# Create start scripts
echo "java -jar waterfall.jar" > bungeecord/start.sh
echo "java -jar paper.jar nogui" > server1/start.sh
echo "java -jar paper.jar nogui" > server2/start.sh

chmod +x bungeecord/start.sh server1/start.sh server2/start.sh

echo "Setup complete! Start servers with:"
echo "1. cd bungeecord && ./start.sh"
echo "2. cd server1 && ./start.sh"
echo "3. cd server2 && ./start.sh"
echo ""
echo "Then connect to localhost:25577 with the mod installed!"