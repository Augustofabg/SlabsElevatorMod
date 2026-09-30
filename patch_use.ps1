 = @(
    'd:\openslabs Elevator\fabric-1.21.1\src\main\java\net\openslabs\elevatorslabs\block\ElevatorSlabBlock.java',
    'd:\openslabs Elevator\neoforge-1.21.1\src\main\java\net\openslabs\elevatorslabs\block\ElevatorSlabBlock.java',
    'd:\openslabs Elevator\forge-1.20.1\src\main\java\net\openslabs\elevatorslabs\block\ElevatorSlabBlock.java'
)

foreach ( in ) {
    if (Test-Path ) {
         = Get-Content  -Raw
        
        # Replace player.openMenu(slabTile) with setting targeted half first
         =  -replace 'player\.openMenu\(slabTile\);', 'slabTile.setLastTargetedTopHalf(localY > 0.5D);
                        player.openMenu(slabTile);'
        
        Set-Content -Path  -Value  -NoNewline
        Write-Host "Patched openMenu in "
    }
}
