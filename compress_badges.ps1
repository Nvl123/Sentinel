Add-Type -AssemblyName System.Drawing
$drawableDir = "app/src/main/res/drawable"
if (-not (Test-Path $drawableDir)) {
    $drawableDir = "src/main/res/drawable"
}

if (-not (Test-Path $drawableDir)) {
    Write-Error "Drawable directory not found!"
    exit
}

$files = Get-ChildItem -Path $drawableDir -Filter "object_illustration_*.png" | Where-Object { $_.Name -notlike "*_small.png" }

foreach ($file in $files) {
    try {
        $img = [System.Drawing.Image]::FromFile($file.FullName)
        $newHeight = 128
        $newWidth = 128
        
        $bmp = New-Object System.Drawing.Bitmap($newWidth, $newHeight)
        $g = [System.Drawing.Graphics]::FromImage($bmp)
        
        $g.InterpolationMode = [System.Drawing.Drawing2D.InterpolationMode]::HighQualityBicubic
        $g.SmoothingMode = [System.Drawing.Drawing2D.SmoothingMode]::HighQuality
        $g.PixelOffsetMode = [System.Drawing.Drawing2D.PixelOffsetMode]::HighQuality
        $g.CompositingQuality = [System.Drawing.Drawing2D.CompositingQuality]::HighQuality

        $g.DrawImage($img, 0, 0, $newWidth, $newHeight)
        
        $targetName = $file.BaseName + "_small.png"
        $targetPath = Join-Path $drawableDir $targetName
        
        $bmp.Save($targetPath, [System.Drawing.Imaging.ImageFormat]::Png)
        
        $g.Dispose()
        $bmp.Dispose()
        $img.Dispose()
        
        Write-Host "SUCCESS: Compressed $($file.Name) -> $targetName"
    } catch {
        Write-Error "FAILED to compress $($file.Name): $($_.Exception.Message)"
    }
}
