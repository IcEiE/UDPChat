# UDPChat
Assignment for Distributed Systems
issa 0 /tell ak hej
issa 0 /all hej
issa 0 /connect

Om ngn disconnectar ska deras namn från PacketHandlerna i clienter försvinna?
Om vi crashar ska vi kunna gå in igen.
Rensa packethandler i clienter för de som försvunnit
Server gör kontroll över vilka som finns kvar

Lösning: Servern skickar två typer av meddelanden. En som checkar om de är där, och en som säger åt dom att ta bort en viss client.

Servern, lyssnar efter meddelanden. Om den time-outar så ska den utföra check på vilka som är kvar.

Varje ClientConnection har en boolean value, som indikerar om de är aktiva eller inte. 
