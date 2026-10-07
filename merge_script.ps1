$metadataPath = "C:\Users\HgBao\Downloads\spotify_million_playlist_dataset\songs_metadata.csv"
$gdrivePath = "C:\Users\HgBao\Downloads\Google_Drive_Songs.csv"
$outputPath = "C:\Users\HgBao\Downloads\FINAL_DATASET.csv"

Write-Host "Dang doc file..."
$metadata = Import-Csv -Path $metadataPath
$gdrive = Import-Csv -Path $gdrivePath

$hash = @{}
foreach ($row in $metadata) {
    $key = $row.track_id -replace 'spotify:track:', 'spotify_'
    $hash[$key] = @{
        title = $row.track_name
        artist = $row.artist_name
    }
}

$output = @()
foreach ($row in $gdrive) {
    $key = $row.title
    if ($hash.ContainsKey($key)) {
        $realTitle = $hash[$key].title
        $realArtist = $hash[$key].artist
    } else {
        $realTitle = $key
        $realArtist = "Unknown Artist"
    }

    $output += [PSCustomObject]@{
        title = $realTitle
        artist = $realArtist
        audio_url = $row.audio_url
    }
}

$output | Export-Csv -Path $outputPath -NoTypeInformation -Encoding UTF8
Write-Host "THANH CONG! File duoc luu tai: $outputPath"